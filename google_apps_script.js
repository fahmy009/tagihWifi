/**
 * Google Apps Script Backend (v5 - Customer & Bill Management)
 * Tables: Users, Customers, Bills
 */

const SPREADSHEET_ID = SpreadsheetApp.getActiveSpreadsheet().getId();

function doGet(e) {
  const action = e.parameter.action;
  const table = e.parameter.table;

  try {
    switch (action) {
      case 'init': return jsonResponse(initializeDatabase());
      case 'getUsers': return jsonResponse(getData('Users'));
      case 'getCustomers': return jsonResponse(getData('Customers'));
      case 'getBills': return jsonResponse(getData('Bills'));
      case 'add': return jsonResponse(addData(table, JSON.parse(e.parameter.data)));
      case 'update': return jsonResponse(updateData(table, JSON.parse(e.parameter.data)));
      case 'delete': return jsonResponse(deleteData(table, e.parameter.id));
      case 'deleteByPeriod': return jsonResponse(deleteByPeriod(e.parameter.month, e.parameter.year));
      default: return jsonResponse({ error: 'Invalid action' });
    }
  } catch (error) {
    return jsonResponse({ error: error.toString() });
  }
}

function doPost(e) { return doGet(e); }

function addData(tableName, data) {
  const lock = LockService.getScriptLock();
  try {
    lock.waitLock(30000); // Wait up to 30 seconds

    const ss = SpreadsheetApp.openById(SPREADSHEET_ID);
    const sheet = ss.getSheetByName(tableName);
    const allData = sheet.getDataRange().getValues();
    const headers = allData[0];

    // Unique username check for Users table
    if (tableName === 'Users') {
      const usernameIndex = headers.indexOf('username');
      const exists = allData.slice(1).some(row => row[usernameIndex] === data.username);
      if (exists) return { status: 'error', message: 'Username sudah ada!' };

      // Generate Unique User Code if not present
      if (!data.userCode) {
        data.userCode = "USR" + Math.random().toString(36).substr(2, 6).toUpperCase();
      }
    }

    let maxId = 0;
    if (allData.length > 1) {
      maxId = Math.max(...allData.slice(1).map(row => Number(row[0]) || 0));
    }
    data.id = maxId + 1;

    const rowData = headers.map(header => data[header] !== undefined ? data[header] : "");
    sheet.appendRow(rowData);

    // Notification for new Bill
    if (tableName === 'Bills') {
      try {
        const adminEmail = Session.getEffectiveUser().getEmail();
        const subject = "Inputan Baru: Tagihan " + data.customerName;
        const body = `Ada inputan tagihan baru:\n\nPelanggan: ${data.customerName}\nLokasi: ${data.customerLocation}\nNominal: Rp ${data.amount}\nPetugas: ${data.createdByUsername}\n\nCek spreadsheet untuk detailnya.`;
        MailApp.sendEmail(adminEmail, subject, body);
      } catch (e) {
        console.log("Failed to send email: " + e.toString());
      }
    }

    return { status: 'success', data: data };
  } finally {
    lock.releaseLock();
  }
}

function updateData(tableName, data) {
  const lock = LockService.getScriptLock();
  try {
    lock.waitLock(30000);
    const ss = SpreadsheetApp.openById(SPREADSHEET_ID);
    const sheet = ss.getSheetByName(tableName);
    const allData = sheet.getDataRange().getValues();
    const headers = allData[0];
    const id = data.id;

    if (!id) return { status: 'error', message: 'ID tidak valid' };

    let rowIndex = -1;
    for (let i = 1; i < allData.length; i++) {
      if (allData[i][0] == id) { rowIndex = i + 1; break; }
    }
    if (rowIndex === -1) return { error: 'Not found' };

    // Prevent duplicate username on update
    if (tableName === 'Users' && data.username) {
      const usernameIndex = headers.indexOf('username');
      for (let i = 1; i < allData.length; i++) {
        if (allData[i][usernameIndex] === data.username && allData[i][0] != id) {
          return { status: 'error', message: 'Username sudah digunakan user lain!' };
        }
      }
    }

    headers.forEach((header, index) => {
      if (data.hasOwnProperty(header) && header !== 'id') {
        sheet.getRange(rowIndex, index + 1).setValue(data[header]);
      }
    });
    return { status: 'success' };
  } finally {
    lock.releaseLock();
  }
}

function deleteData(tableName, id) {
  const ss = SpreadsheetApp.openById(SPREADSHEET_ID);
  const sheet = ss.getSheetByName(tableName);
  const allData = sheet.getDataRange().getValues();
  let rowIndex = -1;
  for (let i = 1; i < allData.length; i++) {
    if (allData[i][0] == id) { rowIndex = i + 1; break; }
  }
  if (rowIndex === -1) return { error: 'Not found' };
  sheet.deleteRow(rowIndex);
  return { status: 'success' };
}

function deleteByPeriod(month, year) {
  const ss = SpreadsheetApp.openById(SPREADSHEET_ID);
  const sheet = ss.getSheetByName('Bills');
  const allData = sheet.getDataRange().getValues();
  const headers = allData[0];
  const dateIndex = headers.indexOf('date');

  if (dateIndex === -1) return { error: 'Date column not found' };

  let rowsDeleted = 0;
  // Iterate backwards to avoid index shifting issues
  for (let i = allData.length - 1; i >= 1; i--) {
    const timestamp = allData[i][dateIndex];
    const date = new Date(timestamp);
    if (date.getMonth() == month && date.getFullYear() == year) {
      sheet.deleteRow(i + 1);
      rowsDeleted++;
    }
  }
  return { status: 'success', deletedCount: rowsDeleted };
}

function initializeDatabase() {
  const ss = SpreadsheetApp.openById(SPREADSHEET_ID);

  const tables = {
    'Users': [['id', 'userCode', 'username', 'password', 'role'], [1, 'USR-SA', 'superadmin', '123', 'SUPERADMIN'], [2, 'USR-AD', 'admin', '123', 'ADMIN'], [3, 'USR-01', 'user1', '123', 'USER']],
    'Customers': [['id', 'name', 'location', 'installationDate'], [1, 'Budi Santoso', 'Jakarta Selatan', '2024-01-01']],
    'Bills': [['id', 'customerName', 'customerLocation', 'amount', 'date', 'createdByUserId', 'createdByUsername']]
  };

  for (let tableName in tables) {
    let sheet = ss.getSheetByName(tableName);
    if (sheet) {
      sheet.clear();
    } else {
      sheet = ss.insertSheet(tableName);
    }
    const values = tables[tableName];
    sheet.getRange(1, 1, values.length, values[0].length).setValues(values);
  }

  const sheet1 = ss.getSheetByName("Sheet1");
  if (sheet1 && ss.getSheets().length > 1) {
    try { ss.deleteSheet(sheet1); } catch(e) {}
  }

  return { status: 'success' };
}

function getData(sheetName) {
  const ss = SpreadsheetApp.openById(SPREADSHEET_ID);
  const sheet = ss.getSheetByName(sheetName);
  if (!sheet) return [];
  const data = sheet.getDataRange().getValues();
  if (data.length <= 1) return [];
  const headers = data[0];
  return data.slice(1).map(row => {
    let obj = {};
    headers.forEach((h, i) => {
      let val = row[i];
      if (val instanceof Date) {
        val = Utilities.formatDate(val, ss.getSpreadsheetTimeZone(), "yyyy-MM-dd");
      }
      obj[h] = val;
    });
    return obj;
  });
}

function jsonResponse(data) {
  return ContentService.createTextOutput(JSON.stringify(data)).setMimeType(ContentService.MimeType.JSON);
}
