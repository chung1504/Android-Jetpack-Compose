// const {onCall} = require("firebase-functions/v2/https");
// const {onDocumentWritten} = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");
const { setGlobalOptions } = require("firebase-functions");
const logger = require("firebase-functions/logger");
const functions = require("firebase-functions");
const { onDocumentCreated } = require("firebase-functions/firestore");
const { initializeApp } = require("firebase-admin/app");


initializeApp();
setGlobalOptions({ maxInstances: 5 });


// hàm tự gửi tin nhắn đi khi có tin nhắn mới được lưu vào db trên server
// Khởi tạo Admin SDK nếu chưa có
if (!admin.apps.length) {
  admin.initializeApp();
}

exports.sendPrivateNotification = onDocumentCreated(
  "chats/{chatId}/messages/{messageId}",
  async (event) => {
    const newMessage = event.data.data();

    if (!newMessage) {
      logger.error("Message data null");
      return null;
    }

    const receiverId = newMessage.receiverId;
    const senderName = newMessage.senderName;
    const chatId = event.params.chatId;

    logger.log("New message:", newMessage);
    logger.log("ReceiverId:", receiverId);

    try {
      const db = admin.firestore();

      // 🔥 1. Query user theo field userId
      const userQuerySnapshot = await db
        .collection("users")
        .where("userId", "==", receiverId)
        .limit(1)
        .get();

      if (userQuerySnapshot.empty) {
        logger.error("❌ Không tìm thấy user với userId:", receiverId);
        return null;
      }

      // 👉 lấy document đầu tiên
      const userDoc = userQuerySnapshot.docs[0];
      const userData = userDoc.data(); // nguoi nhan

      logger.log("✅ User found:", userData);

      const targetToken = userData?.fcmToken;

      if (!targetToken || typeof targetToken !== "string") {
        logger.error("❌ Token không hợp lệ:", targetToken);
        return null;
      }

      logger.log("FCM Token:", targetToken);

      // 🔥 2. Payload
      const message = {
        token: targetToken,
        notification: {
          title: senderName || "Tin nhắn mới",
          body: newMessage.text || "Đã gửi tệp",
        },
        data: {
          chatId: String(chatId),
          senderId: String(newMessage.senderId),
          type: "chat_message",
        },
      };

      logger.log("FCM Payload:", message);

      // 🔥 3. Send
      const response = await admin.messaging().send(message);

      logger.log("✅ Gửi thành công:", response);
    } catch (error) {
      logger.error("❌ Lỗi gửi FCM:", error);

      if (error.code === "messaging/registration-token-not-registered") {
        logger.warn("⚠️ Token hết hạn → xóa");

        // ⚠️ phải query lại để biết docId
        const db = admin.firestore();

        const snapshot = await db
          .collection("users")
          .where("userId", "==", receiverId)
          .limit(1)
          .get();

        if (!snapshot.empty) {
          const docId = snapshot.docs[0].id;

          await db.collection("users").doc(docId).update({
            fcmToken: admin.firestore.FieldValue.delete(),
          });
        }
      }
    }

    return null;
  },
);

// hàm bổ trợ tạo chatId
function generateChatId(user1, user2) {
  return [user1, user2].sort().join("_");
}

// hàm lấy thông tin user từ userId
async function getUserByUserId(userId) {
  const snapshot = await admin
    .firestore()
    .collection("users")
    .where("userId", "==", userId)
    .limit(1)
    .get();

  if (snapshot.empty) {
    throw new Error(`User not found: ${userId}`);
  }

  return snapshot.docs[0].data();
}

// hàm lưu thông tin người dùng vào firestore
exports.saveUserInfo = functions.https.onRequest(async (req, res) => {
  if (req.method !== "POST") {
    return res.status(405).send("Method Not Allowed");
  }

  try {
    const { userId, fullName, email, avatarUrl, fcmToken } = req.body;
    // validate input
    if (!userId || !fcmToken) {
      return res.status(400).json({
        success: false,
        errorMessage: "Missing required fields",
      });
    }

    // lấy thông tin user
    const userRef = admin.firestore().collection("users").doc(userId);
    const userDoc = await userRef.get();

    // TH1: user existed, chỉ update fcmToken, khi user đăng nhập thiết bị mới
    if (userDoc.exists) {
      // chỉ khi đăng nhập vào thiết bị mới thì mới update new fcmToken
      if (userDoc.data().fcmToken != fcmToken) {
        await userRef.update({
          fcmToken,
          lastUpdate: admin.firestore.FieldValue.serverTimestamp(),
        });
      }
    } else {
      // TH2: tạo mới hoàn toàn:
      await userRef.set({
        userId,
        fullName: fullName || "",
        email: email,
        avatarUrl: avatarUrl || "",
        fcmToken,
        lastUpdate: admin.firestore.FieldValue.serverTimestamp(),
      });
    }
    // trả về lưu thành công thông tin user
    return res.status(200).json({ success: true, errorMessage: null });
  } catch (error) {
    // trả về thông báo lỗi không thể lưu thông tin user
    console.error(error);
    return res
      .status(500)
      .json({ success: false, errorMessage: error.message });
  }
});

exports.getUserInfo = functions.https.onRequest(async (req, res) => {
  if (req.method !== "POST") return res.status(405).send("Method Not Allowed");

  try {
    const { userId } = req.body;
    if (!userId) {
      return res.status(400).json({
        success: false,
        errorMessage: "Missing userId",
      });
    }

    const currentUser = await getUserByUserId(userId);
    if (!currentUser) {
      return res.status(404).json({
        success: false,
        errorMessage: "User not found",
      });
    }

    return res.status(200).json({
      success: true,
      data: currentUser,
    });
  } catch (error) {
    console.error(error);
    return res.status(500).json({
      success: false,
      errorMessage: error.message,
    });
  }
});

exports.getRecentMessages = functions.https.onRequest(async (req, res) => {
  try {
    const { senderId, receiverId, lastCreatedAt } = req.body;

    if (!senderId || !receiverId) {
      return res.status(400).json({
        success: false,
        message: "senderId và receiverId là bắt buộc",
      });
    }

    const chatId = [senderId, receiverId].sort().join("_");

    const messagesRef = admin
      .firestore()
      .collection("chats")
      .doc(chatId)
      .collection("messages");

    // 🔥 lấy 21 thay vì 20
    let query = messagesRef.orderBy("createdAt", "desc").limit(21);

    if (lastCreatedAt) {
      const cursor = admin.firestore.Timestamp.fromMillis(lastCreatedAt);
      query = query.startAfter(cursor);
    }

    const snapshot = await query.get();

    let docs = snapshot.docs;

    // ✅ check hasMore
    const hasMore = docs.length > 20;

    // chỉ lấy đúng 20 item trả về
    if (hasMore) {
      docs = docs.slice(0, 20);
    }

    const messages = docs.map((doc) => {
      const data = doc.data();
      return {
        id: doc.id,
        ...data,
        createdAt: data.createdAt?.toMillis() || null,
      };
    });

    return res.status(200).json({
      success: true,
      chatId,
      hasMore, // thêm field này
      data: messages,
    });
  } catch (error) {
    console.error("getRecentMessages error:", error);
    return res.status(500).json({
      success: false,
      message: error.message,
    });
  }
});

// hàm gửi tin nhắn truyền thống
exports.sendMessageRest = functions.https.onRequest(async (req, res) => {
  if (req.method !== "POST") {
    return res.status(405).send("Method Not Allowed");
  }

  try {
    const { senderId, receiverId, text } = req.body;

    // ✅ Validate input
    if (!senderId || !receiverId || !text) {
      return res.status(400).send({
        status: "Error",
        msg: "Missing required fields: senderId, receiverId, text",
      });
    }

    // ✅ Generate chatId
    const chatId = generateChatId(senderId, receiverId);

    // ✅ Lấy sender info
    const sender = await getUserByUserId(senderId);

    const senderName = sender.fullName;

    // ✅ Lưu message
    await admin
      .firestore()
      .collection("chats")
      .doc(chatId)
      .collection("messages")
      .add({
        senderId,
        receiverId,
        senderName,
        text,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
      });

    return res.status(200).send({
      status: "Success",
      chatId,
      msg: "Message stored",
    });
  } catch (error) {
    console.error(error);
    return res.status(500).send({
      status: "Error",
      msg: error.message,
    });
  }
});

// hàm gửi tin v2
exports.apiSendMessage = functions.https.onCall(async (data, context) => {
  // 1. Kiểm tra xác thực (Chỉ người dùng đã đăng nhập mới được gọi)
  if (!context.auth) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "Bạn cần đăng nhập!",
    );
  }

  const { chatId, receiverId, text, senderName } = data;
  const senderId = context.auth.uid;

  const messagesRef = admin
    .firestore()
    .collection("chats")
    .doc(chatId)
    .collection("messages");

  const newMessage = {
    senderId,
    receiverId,
    senderName,
    text,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  };

  try {
    await messagesRef.add(newMessage);
    return { success: true, message: "Đã gửi tin nhắn thành công" };
  } catch (error) {
    throw new functions.https.HttpsError("internal", error.message);
  }
});
