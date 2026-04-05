const admin = require("firebase-admin")
const functions = require("firebase-functions")
const { onCall, HttpsError } = require("firebase-functions/v2/https")

/**
 * [onRequest] Lấy tin nhắn gần nhất, có phân trang
 * Method: POST
 * Body: { senderId, receiverId, lastCreatedAt? }
 */
// * Hàm lấy tin nhắn gần đây nhất bằng cáh REST API
exports.getRecentMessages = functions.https.onRequest(async (req, res) => {
    try {

    } catch (error) {
           
    }
})
