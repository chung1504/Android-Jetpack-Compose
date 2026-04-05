/*
 * require: cách import của nodejs
 * firebase-admin: là thư viện backend nói chuyện với firebase, có quyền mạnh hơn client (ghi/xóa DB thoải mái)
 */
const admin = require("firebase-admin");

/*
 * đây là cách viết destructing, {} = “lấy ra từng function trong object”
 * Nếu viết đầy đủ nó sẽ là
 * const functions = require("firebase-functions");
 * const setGlobalOptios = functions.setGlobalOptions;
 * nghĩa là lấy đúng hàm setGlobalOptions ra dùng luôn
 */
// * setGlobalOptions: dùng để set cấu hình mặc định cho toàn bộ function.
const { setGlobalOptions } = require("firebase-functions");

// * Bắt buộc, nuế k có k truy cập được firestore, k dùng đc auth, khong gửi notification được
admin.initializeApp();

// * Cáu hình tại vị trí chạy ở Singapore và
// * Giới hạn tối đa 5 function chạy cùng lúc
setGlobalOptions({
  region: "asia-southeast1",
  maxInstances: 5,
});

// Import file riêng (module hóa code) ,liên kết tới các file
const { sendPrivateNotification } = require("./src/notification");
const { saveUserInfo, getUserInfo } = require("./src/user");
const { getRecentMessages } = require("./src/message");

// exports: dăng kí function này cho firebase
// Viết tắt của module.exports
exports.sendPrivateNotification = sendPrivateNotification;
exports.saveUserInfo = saveUserInfo;
exports.getUserInfo = getUserInfo;
exports.getRecentMessages = getRecentMessages;

/*
Phân tích các hàm
* 1. sendPrivateNotification — trigger tự động khi có message mới trong Firestore, tìm fcmToken của người nhận và gửi push notification. Đây là hàm quan trọng nhất, viết tốt, có xử lý token hết hạn.

* 2. saveUserInfo — lưu thông tin user vào Firestore sau khi đăng nhập. Logic đúng: nếu user đã tồn tại thì chỉ update fcmToken khi thay đổi, nếu chưa có thì tạo mới.

* 3. getUserInfo — lấy thông tin user theo userId. Đơn giản, ổn.

* 4. getRecentMessages — lấy 20 tin nhắn gần nhất, có phân trang bằng lastCreatedAt, có hasMore. Logic lấy 21 để check hasMore là trick hay.

* 5. sendMessageRest — gửi tin nhắn qua REST. Hàm này không cần thiết vì bạn đã có sendPrivateNotification trigger tự động. Nếu app Android ghi thẳng vào Firestore thì notification tự chạy, không cần đi qua REST.

* 6. apiSendMessage — gửi tin nhắn qua callable function, có check authentication. Hàm này tốt hơn sendMessageRest vì có auth, nhưng cũng không cần nếu app ghi thẳng Firestore.
*/
