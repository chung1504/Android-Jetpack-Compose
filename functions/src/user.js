const admin = require("firebase-admin");
const functions = require("firebase-functions");
const {
  onCall,
  HttpsError,
} = require("firebase-functions/v2/https");
const { getUserByUserId } = require("../utils/firestore");
const { user } = require("firebase-functions/v1/auth");
const { Suspense } = require("react");

// ============================================================
// * CÁCH 1: onRequest — giống REST API truyền thống
// ============================================================
// Ưu điểm:
//   - Quen thuộc, dễ test bằng Postman/curl
//   - Gọi được từ bất kỳ ngôn ngữ nào qua HTTP
// Nhược điểm:
//   - Phải tự xử lý CORS nếu gọi từ web
//   - Phải tự parse req.body, tự trả res
//   - Không tự động có auth — phải tự verify token nếu cần bảo mật
//   - Phải check req.method thủ công
// ============================================================

/**
 * [onRequest] Lưu thông tin người dùng vào Firestore
 * Method: POST
 * Body: { userId, fullName, email, avatarUrl, fcmToken }
 */

// * Hàm lưu thông tin người dùng
exports.saveUserInfo = functions.https.onRequest(async (req, res) => {
  // onRequest nhận 2 tham số:
  //   req — HTTP request (method, body, headers, query...)
  //   res — HTTP response (dùng để trả về kết quả)

  // Chỉ cho phép POST, trả 405 nếu method khác
  if (req.method !== "POST") {
    return res.status(405).send("Method Not Allowed");
  }

  try {
    // Dùng destructure body để lấy từng function
    // Dữ liệu client gửi lên nằm trong req.body
    const { usedId, fullName, email, avatarUrl, fcmToken } = req.body;

    // Validate(xác nhận): userId và fcmToken là bắt buộc
    if (!usedId || !fcmToken) {
      // return status(400) = Bad request - client gửi thiếu dữ liệu
      return res.status(400).json({
        success: false,
        errorMessage: "Missing requied fields: userId or fcmToken",
      });
    }

    // Tạo reference đến document users/{userId}
    // .doc(userId) dùng userId làm document ID luôn — truy xuất O(1)
    const userRef = admin.firestore().collection("users").doc(usedId);

    // .get() để đọc document - trả về DocumentSnapshot
    const userDoc = await userRef.get();

    if (userDoc.exists) {
      // TH1: User đã tồn tại
      // Chỉ update fcmToken nếu khác — tránh write không cần thiết
      // (mỗi write Firestore tốn tiền khi vượt free tier)
      if (userDoc.data().fcmToken !== fcmToken) {
        // Nếu dữ liệu fcm hiện có khác với fcm mới thì update
        await userRef.update({
          fcmToken,
          // serverTimestamp() — Firebase tự điền thời gian server, không dùng Date.now()
          lastUpdate: admin.firestore.FieldValue.serverTimestamp(),
        });
      }
    } else {
      // TH2: User chưa tồn tại → tạo mới bằng .set()
      // .set() ghi đè hoàn toàn document (khác .update() chỉ update field được chỉ định)
      await userRef.set({
        usedId,
        fullName: fullName || "",
        email: email || "",
        avatarUrl: avatarUrl || "",
        fcmToken,
        lastUpdate: admin.firestore.FieldValue.serverTimestamp(),
      });
    }

    // Cập nhật xong rồi trả về trạng thái status(200) = thành công
    return res.status(200).json({
      success: true,
      errorMessage: null,
    });
  } catch (error) {
    // Nếu lỗi thì in ra lỗi
    console.error("saveUserInfo error: ", error);
    // Và trả về status(500) = lỗi server với thong tin json
    return res.status(500).json({
      success: false,
      errorMessage: error.message,
    });

    // Nếu tìm thấy thì trả về dư liệu
    return res.status(200).json({
      success: true,
      errorMessage: null,
    });
  }
});

/**
 * [onRequest] Lấy thông tin người dùng
 * Method: POST
 * Body: { userId }
 */
// * Hàm lấy thông tin người dùng
exports.getUserInfo = functions.https.onRequest(async (req, res) => {
  if (req.method !== "POST") {
    return res.status(405).send("method not allowed");
  }

  try {
    const { usedId } = req.body;

    if (!usedId) {
      return res.status(400).json({
        success: false,
        errorMessage: "Missing userId",
      });
    }

    const user = await getUserByUserId(usedId);

    if (!user) {
      // status (404) = k tìm thấy
      return res.status(404).json({
        success: false,
        errorMessage: "user not found",
      });
    }
  } catch (error) {
    console.error("getUsetInfo error: ", error);
    return res.status(500).json({
      success: false,
      errorMessage: error.message,
    });
  }
});

// ============================================================
// CÁCH 2: onCall — cách Firebase khuyến nghị
// ============================================================
// Ưu điểm:
//   - Tự xử lý CORS hoàn toàn
//   - Tự deserialize JSON — nhận data thẳng, không cần parse
//   - Tự có auth: context.auth chứa uid, email của người gọi
//   - Ném HttpsError thay vì res.status() — client bắt được kiểu lỗi
//   - Firebase SDK trên Android/iOS gọi trực tiếp, không cần biết URL
// Nhược điểm:
//   - Khó test bằng Postman (phải dùng Firebase Emulator hoặc SDK)
//   - Chỉ gọi được từ Firebase SDK — không phải REST thuần
// ============================================================

/**
 * [onCall] Lưu thông tin người dùng
 * Gọi từ Android: functions.getHttpsCallable("saveUserInfo").call(data)
 * data: { userId, fullName, email, avatarUrl, fcmToken }
 */
// * Hàm lưu thong tin bằng cách onCall
exports.saveUserInfoByOnCall = onCall(async (request) => {
  // onCall chỉ nhận 1 tham số: request
  //   request.data   — dữ liệu client gửi lên (đã được deserialize tự động)
  //   request.auth   — thông tin người dùng đã đăng nhập (uid, email, token)
  //                    null nếu chưa đăng nhập
  //   request.rawRequest — HTTP request gốc nếu cần
  // Kiểm tra người dùng đã đăng nhập chưa
  // Đây là điểm mạnh của onCall — auth có sẵn, không cần verify token thủ công
  if (!request.auth) {
    // HttpsError thay thế res.status() — client nhận được error code chuẩn
    throw new HttpError("unauthenticated", "Bạn cần đăng nhập");
  }

  // Lấy data trực tiếp từ request.data — không cần req.body hay JSON.parse
  /*
    * Viết rõ là 
    const data = request.data;

    const userId = data.userId;
    const fullName = data.fullName;
    const email = data.email;
    const avatarUrl = data.avatarUrl;
    const fcmToken = data.fcmToken;
  */
  const { userId, fullName, email, avatarUrl, fcmToken } = request.data;

  // Validate
  if (!usedId || !fcmToken) {
    // "invalid-argument" = tương đương với HTTP 400
    throw new HttpsError("invalib-argument", "Thiếu userId hoặc fcmToken");
  }

  try {
    const userRef = admin.firestore().collection("users").doc(usedId);
    const userDoc = await userRef.get();

    if (userDoc.exists) {
      // Nếu tồn tại rồi thì chỉ update fcmToken khi thay đỏi
      if (userDoc.data().fcmToken != fcmToken) {
        await userRef.update({
          fcmToken,
          lastUpdate: admin.firestore.FieldValue.serverTimestamp(),
        });
      }
    } else {
      await userRef.set({
        userId,
        fullName: fullName || "",
        email: email || "",
        avatarUrl: avatarUrl || "",
        fcmToken,
        lastUpdate: admin.firestore.FieldValue.serverTimestamp(),
      });
    }

    // onCall trả về object trực tiếp — không cần res.json()
    // Firebase SDK tự wrap thành response cho client
    return {
      success: true,
      errorMessage: null,
    };
  } catch (error) {
    console.error("saveUserInfo error:", error);
    // "internal" = tương đương với http 500
    throw new HttpsError("internal", error.message);
  }
});

/**
 * [onCall] Lấy thông tin người dùng
 * data: { userId }
 */
// * Hàm lấy thong tin bằng onCall
exports.getUserInfoByOnCall = onCall(async (request) => {
  if (!request.auth) {
    // HttpsError thay thế res.status() — client nhận được error code chuẩn
    throw new HttpError("unauthenticated", "Bạn cần đăng nhập");
  }

  const { userId } = request.data

  if (!userId) {
    throw new HttpsError("invalid-argument", "Thiếu userId")
  }

  try {
    const user = await getUserByUserId(userId)

    if (!user) {
        // "not-found" = tương đương HTTP 404 
        throw new HttpsError("not-found", "Không tìm thấy user")
    }

    return {
        success: true,
        data: user
    }
  } catch (error) {
    // Nếu error đã là HttpsError thì ném lại nguyên vẹn 
    if (error instanceof HttpsError) throw error;
    throw new HttpsError("internal", error.message)
  }
}); 

// ============================================================
// SO SÁNH NHANH
// ============================================================
//
//                  onRequest          onCall
// Auth             ❌ tự làm          ✅ tự động (request.auth)
// CORS             ❌ tự xử lý        ✅ tự động
// Parse body       ❌ req.body        ✅ request.data
// Trả kết quả      res.json()         return object
// Xử lý lỗi        res.status(xxx)    throw HttpsError
// Test Postman      ✅ dễ             ❌ khó hơn
// Gọi từ SDK        ❌ cần URL        ✅ chỉ cần tên hàm
//
// → Kết luận: Dùng onCall cho app mobile/web Firebase
//             Dùng onRequest nếu cần expose API cho bên thứ 3
// ============================================================
