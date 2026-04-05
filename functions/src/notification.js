const { onDocumentCreated } = require("firebase-functions/firestore");
const admin = require("firebase-admin");
const logger = require("firebase-functions/logger");

exports.sendPrivateNotification = onDocumentCreated(
  "chats/{chatId}/messages/{messageId}",
  async (event) => {
    // ... giữ nguyên code cũ
  },
);
