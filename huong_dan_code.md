# HƯỚNG DẪN TÍCH HỢP QUẢN LÝ GIAO DỊCH CỦA TỪNG USER (BACKEND & FRONTEND)

Tài liệu này hướng dẫn chi tiết cách tự code tính năng quản lý giao dịch **theo từng User cụ thể** (Xem chi tiết lịch sử nạp xu và mua chương của một user bất kỳ từ trang Admin).

---

## MỤC LỤC
1. [Mô Tả Nghiệp Vụ Mới](#1-mô-tả-nghiệp-vụ-mới)
2. [Thiết Kế API Endpoints](#2-thiết-kế-api-endpoints)
3. [Hướng Dẫn Code Backend (Java)](#3-hướng-dẫn-code-backend-java)
   - [Bước 3.1: Viết Repository](#bước-31-viết-repository)
   - [Bước 3.2: Viết Service](#bước-32-viết-service)
   - [Bước 3.3: Viết Controller](#bước-33-viết-controller)
4. [Hướng Dẫn Code Frontend (React)](#4-hướng-dẫn-code-frontend-react)
   - [Bước 4.1: Thêm API Endpoints & Service](#bước-41-thêm-api-endpoints--service)
   - [Bước 4.2: Tích hợp nút "Xem Giao dịch" vào AdminPage](#bước-42-tích-hợp-nút-xem-giao-dịch-vào-adminpage)
   - [Bước 4.3: Viết Component hiển thị lịch sử của User](#bước-43-viết-component-hiển-thị-lịch-sử-giao-dịch-user)

---

## 1. MÔ TẢ NGHIỆP VỤ MỚI

Thay vì hiển thị một danh sách giao dịch chung chung của toàn bộ hệ thống, tính năng này được thiết kế tập trung để Admin quản lý giao dịch của **từng User riêng biệt**:
- Tại màn hình **Quản lý người dùng** (Admin), cạnh mỗi User sẽ có thêm nút hành động **"Xem Giao dịch"**.
- Khi Admin nhấn nút này, một hộp thoại (Modal) hoặc trang chi tiết sẽ xuất hiện, hiển thị:
  1. **Lịch sử Nạp xu** (Thông qua thanh toán VNPay) của riêng người dùng đó.
  2. **Lịch sử Mở khóa chương** (Tiêu xu) của riêng người dùng đó.
- Điều này giúp Admin dễ dàng đối soát tài khoản, kiểm tra số dư và hỗ trợ người dùng khi có khiếu nại về nạp tiền hoặc mua chương sách.

---

## 2. THIẾT KẾ API ENDPOINTS

Hệ thống sẽ cung cấp 4 endpoint chính (2 cho User tự xem của mình, 2 cho Admin xem của User khác):

### Cho User tự xem:
- `GET /api/payment/my-history?page=0&size=10`: Lấy lịch sử nạp tiền của tôi.
- `GET /api/chapters/my-unlocks?page=0&size=10`: Lấy lịch sử mua chương của tôi.

### Cho Admin quản lý (yêu cầu quyền SCOPE_ADMIN):
- `GET /api/payment/admin/user/{userId}?page=0&size=10`: Lấy lịch sử nạp tiền của User có ID là `userId`.
- `GET /api/chapters/admin/user/{userId}/unlocks?page=0&size=10`: Lấy lịch sử mua chương của User có ID là `userId`.

---

## 3. HƯỚNG DẪN CODE BACKEND (JAVA)

*Bạn hãy mở các file Backend tương ứng và thêm code:*

### Bước 3.1: Viết Repository

* Mở file **[PaymentRepository.java](file:///d:/Files/ALUANVANTOTNGHIEP/LV_BACKEND/lv_backend/src/main/java/org/example/lv_backend/repository/PaymentRepository.java)** và thêm phương thức:
  ```java
  // Lấy lịch sử nạp tiền của một user cụ thể (dùng chung cho cả User tự xem và Admin tra cứu)
  Page<Payment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
  ```

* Mở file **[ChapterUnlockRepository.java](file:///d:/Files/ALUANVANTOTNGHIEP/LV_BACKEND/lv_backend/src/main/java/org/example/lv_backend/repository/ChapterUnlockRepository.java)** và thêm phương thức:
  ```java
  // Lấy lịch sử chi tiêu mở khóa của một user cụ thể (dùng chung cho cả User tự xem và Admin tra cứu)
  Page<ChapterUnlock> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
  ```

---

### Bước 3.2: Viết Service

* Mở file **[PaymentService.java](file:///d:/Files/ALUANVANTOTNGHIEP/LV_BACKEND/lv_backend/src/main/java/org/example/lv_backend/service/PaymentService.java)** và thêm 2 phương thức:
  ```java
  @Transactional(readOnly = true)
  public Page<PaymentResponse> getMyPaymentHistory(int page, int size) {
      String currentUsername = securityUtil.getCurrentUsername();
      User user = userRepository.findByName(currentUsername)
              .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
      Pageable pageable = PageRequest.of(page, size);
      return paymentRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
              .map(paymentMapper::toPaymentResponse);
  }

  @Transactional(readOnly = true)
  public Page<PaymentResponse> getPaymentsByUserAdmin(Long userId, int page, int size) {
      Pageable pageable = PageRequest.of(page, size);
      return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
              .map(paymentMapper::toPaymentResponse);
  }
  ```

* Mở file **[ChapterUnlockService.java](file:///d:/Files/ALUANVANTOTNGHIEP/LV_BACKEND/lv_backend/src/main/java/org/example/lv_backend/service/ChapterUnlockService.java)** và thêm 2 phương thức:
  ```java
  @Transactional(readOnly = true)
  public Page<ChapterUnlockResponse> getMyUnlockHistory(int page, int size) {
      String currentUsername = securityUtil.getCurrentUsername();
      User user = userRepository.findByName(currentUsername)
              .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
      Pageable pageable = PageRequest.of(page, size);
      return chapterUnlockRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
              .map(chapterUnlockMapper::toChapterUnlockResponse);
  }

  @Transactional(readOnly = true)
  public Page<ChapterUnlockResponse> getUnlocksByUserAdmin(Long userId, int page, int size) {
      Pageable pageable = PageRequest.of(page, size);
      return chapterUnlockRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
              .map(chapterUnlockMapper::toChapterUnlockResponse);
  }
  ```

---

### Bước 3.3: Viết Controller

* Mở file **[PaymentController.java](file:///d:/Files/ALUANVANTOTNGHIEP/LV_BACKEND/lv_backend/src/main/java/org/example/lv_backend/controller/PaymentController.java)** và thêm các endpoint:
  ```java
  @GetMapping("/my-history")
  public ApiResponse<Page<PaymentResponse>> getMyPaymentHistory(
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size) {
      return ApiResponse.<Page<PaymentResponse>>builder()
              .message("Lấy lịch sử giao dịch nạp xu thành công")
              .result(paymentService.getMyPaymentHistory(page, size))
              .build();
  }

  @GetMapping("/admin/user/{userId}")
  @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN')")
  public ApiResponse<Page<PaymentResponse>> getPaymentsByUserAdmin(
          @PathVariable Long userId,
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size) {
      return ApiResponse.<Page<PaymentResponse>>builder()
              .message("Lấy lịch sử giao dịch nạp xu của người dùng thành công")
              .result(paymentService.getPaymentsByUserAdmin(userId, page, size))
              .build();
  }
  ```

* Mở file **[ChapterUnlockController.java](file:///d:/Files/ALUANVANTOTNGHIEP/LV_BACKEND/lv_backend/src/main/java/org/example/lv_backend/controller/ChapterUnlockController.java)** và thêm các endpoint:
  ```java
  @GetMapping("/my-unlocks")
  public ApiResponse<Page<ChapterUnlockResponse>> getMyUnlockHistory(
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size) {
      return ApiResponse.<Page<ChapterUnlockResponse>>builder()
              .message("Lấy lịch sử mở khóa chương sách thành công")
              .result(chapterUnlockService.getMyUnlockHistory(page, size))
              .build();
  }

  @GetMapping("/admin/user/{userId}/unlocks")
  @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN')")
  public ApiResponse<Page<ChapterUnlockResponse>> getUnlocksByUserAdmin(
          @PathVariable Long userId,
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size) {
      return ApiResponse.<Page<ChapterUnlockResponse>>builder()
              .message("Lấy lịch sử mở khóa của người dùng thành công")
              .result(chapterUnlockService.getUnlocksByUserAdmin(userId, page, size))
              .build();
  }
  ```

---

## 4. HƯỚNG DẪN CODE FRONTEND (REACT)

### Bước 4.1: Thêm API Endpoints & Service

* Trong file **`src/services/apiEndpoints.js`**, thêm:
  ```javascript
  CHAPTERS: {
    MY_UNLOCKS: '/api/chapters/my-unlocks',
    ADMIN_USER_UNLOCKS: (userId) => `/api/chapters/admin/user/${userId}/unlocks`,
  },
  PAYMENT: {
    MY_HISTORY: '/api/payment/my-history',
    ADMIN_USER_HISTORY: (userId) => `/api/payment/admin/user/${userId}`,
  }
  ```

* Trong file **`src/services/paymentService.js`**, thêm:
  ```javascript
  getMyPaymentHistory: (page = 0, size = 10) => {
    return apiClient.get(`${API_ENDPOINTS.PAYMENT.MY_HISTORY}?page=${page}&size=${size}`);
  },
  
  getUserPaymentsAdmin: (userId, page = 0, size = 10) => {
    return apiClient.get(`${API_ENDPOINTS.PAYMENT.ADMIN_USER_HISTORY(userId)}?page=${page}&size=${size}`);
  }
  ```

* Trong file **`src/services/chapterService.js`**, thêm:
  ```javascript
  getMyUnlockHistory: async (page = 0, size = 10) => {
    return await apiClient.get(`${API_ENDPOINTS.CHAPTERS.MY_UNLOCKS}?page=${page}&size=${size}`);
  },

  getUserUnlocksAdmin: async (userId, page = 0, size = 10) => {
    return await apiClient.get(`${API_ENDPOINTS.CHAPTERS.ADMIN_USER_UNLOCKS(userId)}?page=${page}&size=${size}`);
  }
  ```

---

### Bước 4.2: Tích hợp nút "Xem Giao dịch" vào `AdminPage.jsx`

Mở file **`src/pages/AdminPage/AdminPage.jsx`**:
- Trong danh sách các cột của bảng người dùng, thêm một nút bấm **"Xem Giao dịch"** ở cột Hành động (Action).
- Khi click nút này, cập nhật trạng thái `selectedUserForTransactions` bằng ID của user và mở một hộp thoại (Modal) hiển thị lịch sử giao dịch.

```jsx
// Ví dụ trong bảng danh sách user:
{results.map(user => (
  <tr key={user.id}>
    <td>{user.email || 'Chưa có email'}</td>
    <td className="user-name-cell">{user.name}</td>
    <td>
      <div style={{ display: 'flex', gap: '8px' }}>
        <ActionButtons
          onEdit={() => handleEditClick(user)}
          onDelete={() => handleDelete(user)}
          showText={true}
        />
        <button 
          onClick={() => setSelectedUserForTransactions(user)}
          style={{ padding: '4px 8px', backgroundColor: '#8b5cf6', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
        >
          Xem Giao dịch
        </button>
      </div>
    </td>
  </tr>
))}
```

---

### Bước 4.3: Viết Component hiển thị lịch sử giao dịch của User

Tạo Component **`UserTransactionModal.jsx`** hiển thị lịch sử giao dịch của User được chọn. Component này sẽ gọi hai API Admin vừa định nghĩa ở trên để hiển thị dữ liệu:

```jsx
import React, { useEffect, useState } from 'react';
import paymentService from '../../services/paymentService';
import chapterService from '../../services/chapterService';

function UserTransactionModal({ user, onClose }) {
  const [tab, setTab] = useState('deposit'); // 'deposit' hoặc 'unlock'
  const [data, setData] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (user) {
      loadTransactions();
    }
  }, [user, tab, page]);

  const loadTransactions = async () => {
    setLoading(true);
    try {
      if (tab === 'deposit') {
        const res = await paymentService.getUserPaymentsAdmin(user.id, page, 10);
        setData(res.result.content || []);
        setTotalPages(res.result.totalPages || 0);
      } else {
        const res = await chapterService.getUserUnlocksAdmin(user.id, page, 10);
        setData(res.result.content || []);
        setTotalPages(res.result.totalPages || 0);
      }
    } catch (err) {
      console.error("Lỗi khi tải giao dịch:", err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-backdrop" style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000 }}>
      <div className="modal-content" style={{ background: '#1e293b', color: '#fff', padding: '20px', borderRadius: '12px', width: '80%', maxWidth: '800px', maxHeight: '90vh', overflowY: 'auto' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '20px' }}>
          <h3>Lịch sử giao dịch: {user.name}</h3>
          <button onClick={onClose} style={{ background: 'none', border: 'none', color: '#fff', cursor: 'pointer', fontSize: '1.2rem' }}>&times;</button>
        </div>

        <div style={{ display: 'flex', gap: '10px', marginBottom: '15px' }}>
          <button onClick={() => { setTab('deposit'); setPage(0); }} style={{ padding: '8px 16px', background: tab === 'deposit' ? '#3b82f6' : '#334155', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Giao dịch Nạp xu</button>
          <button onClick={() => { setTab('unlock'); setPage(0); }} style={{ padding: '8px 16px', background: tab === 'unlock' ? '#3b82f6' : '#334155', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Mở khóa chương</button>
        </div>

        {loading ? (
          <div>Đang tải...</div>
        ) : (
          <>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
              <thead>
                {tab === 'deposit' ? (
                  <tr style={{ borderBottom: '1px solid #334155' }}>
                    <th>Mã GD VNPay</th>
                    <th>Số tiền (VND)</th>
                    <th>Thời gian</th>
                    <th>Trạng thái</th>
                  </tr>
                ) : (
                  <tr style={{ borderBottom: '1px solid #334155' }}>
                    <th>Tên Sách</th>
                    <th>Chương số</th>
                    <th>Giá Xu</th>
                    <th>Thời gian mua</th>
                  </tr>
                )}
              </thead>
              <tbody>
                {data.length === 0 ? (
                  <tr><td colSpan="4" style={{ textAlign: 'center', padding: '20px' }}>Không có giao dịch nào.</td></tr>
                ) : (
                  data.map((item, idx) => (
                    <tr key={idx} style={{ borderBottom: '1px solid #334155' }}>
                      {tab === 'deposit' ? (
                        <>
                          <td>{item.vnpayTxnRef || `REF-${item.id}`}</td>
                          <td>{item.amount.toLocaleString()} VND</td>
                          <td>{new Date(item.createdAt).toLocaleString()}</td>
                          <td>{item.status}</td>
                        </>
                      ) : (
                        <>
                          <td>{item.bookTitle}</td>
                          <td>Chương {item.chapterNumber}: {item.chapterTitle}</td>
                          <td>-{item.price} xu</td>
                          <td>{new Date(item.createdAt).toLocaleString()}</td>
                        </>
                      )}
                    </tr>
                  ))
                )}
              </tbody>
            </table>

            {totalPages > 1 && (
              <div style={{ display: 'flex', justifyContent: 'center', gap: '10px', marginTop: '15px' }}>
                <button disabled={page === 0} onClick={() => setPage(page - 1)}>Trước</button>
                <span>Trang {page + 1} / {totalPages}</span>
                <button disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)}>Sau</button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}

export default UserTransactionModal;
```
