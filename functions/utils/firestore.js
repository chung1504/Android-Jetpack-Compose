// * Dùng truy cập Cloud Firebase , có quyền full
const admin = require("firebase-admin");

// * Tạo id duy nhất cho cuộc chat giữa 2 người
// * A và B -> A_B , nó sắp xếp để dù thứ tự khác vẫn tạo ra 1 id chung
// * Tránh tạo 2 phòng chat khác nhau
function generateChatId(user1, user2) {
  // tạo mảng -> sắp xếp -> nối thành chuỗi
  return [user1, user2].sort.join("_");
}

// * async = dùng await (bất đòng bộ)
async function getUserByUserId(userId) {
  const snapshot = await admin
    .firestore() // Truy cập vào firestore database
    .collection("users") // chọn collection users
    .where("userId", "==", userId) // query: lấy userId = giá trị truyền vào
    .limit(1) // Chỉ lấy 1 kết quả
    .get(); // thực thi query , trả về snapshot

  // Nếu dữ liệu trả về trống thì trả về ngoại lệ
  if (snapshot.empty) throw new Error(`User not found: ${userId}`);
  // Lấy document đầu tiên rồi lấy data (json)
  return snapshot.docs[0].data();
}

// File này export 2 hàm này , nếu bên file khác dùng thìviết
// * const { generateChatId, getUserByUserId } = require("./firestore");
module.exports = { generateChatId, getUserByUserId };
/*
 * Nó giống :
 * module.exports = {
 *  generateChatId: function() {}
 *  getUserByUserId: function() {}
 * }
 */
