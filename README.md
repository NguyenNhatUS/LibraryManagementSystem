# Bài tập Seminar: Hệ thống quản lý thư viện
- Môn học: Lập trình ứng dụng Java
Lớp: CQ2024-7
- Giảng viên lý thuyết: ThS Nguyễn Văn Khiết
- Giảng viên thực hành: ThS Mai Anh Tuấn và ThS Hồ Tuấn Thanh
- Liên hệ hỏi đáp bài tập: khuyến khích hỏi trên Moodle hoặc hỏi qua email htthanh@fit.hcmus.edu.vn (với các câu hỏi riêng tư)

# Yêu cầu chung
- Đây là bài tập cá nhân, mỗi SV tự làm. Các trường hợp giống bài của nhau, giống bài làm trên Internet (ở một mức độ nhất định) sẽ tính là gian lận trong học tập, dẫn đến kết quả là 0đ toàn bộ môn học.
- SV cần nộp những tài liệu sau đây:
    + Toàn bộ source code bài làm.
    + File JAR có thể chạy được.
    + File data chứa dữ liệu để demo tất cả các chức năng của đề tài. Với chức năng nào mà không có sẵn demo data, xem như chức năng đó không hoàn thành.
    + Một file báo cáo quá trình làm việc, viết bằng định dạng Markdown, sau đó convert sang PDF (nộp file Markdown + file PDF) chứa các nội dung sau đây: MSSV, Họ tên, Mức độ sử dụng AI trong project (0% -> 100%), Tuyên bố sử dụng AI theo mẫu A hoặc mẫu B (xem file AI Usage Guideline), Một bảng đánh giá các chức năng, các công việc đánh hoàn thành, gồm các cột: Tên tính năng, Mức độ hoàn thành (0% - 100%), danh sách (hình chụp) các Git commits cho tính năng đó. Một commit chỉ dành cho 1 tính năng. Một tính năng có thể có nhiều commit.
    + Link Youtube Playlist (mode=Unlist hoặc Public) để demo hướng dẫn sử dụng từng tính năng.
- SV nếu thiếu một trong các tài liệu trên, sẽ được chấm 0đ bài tập này.
- Bài làm được zip lại thành file có định dạng MSSV.zip. Link nộp bài nộp được tối đa 20 files, mỗi file tối đa 20MB => bài nộp tối đa 200MB. Nếu 1 file zip bài làm quá 20MB, SV có thể dùng các tính năng zip and split trên WinRar (hoặc các app tương tự) để split ra nhiều part khi nộp. Các bài nộp link Google Drive, One Drive, Dropbox sẽ không được chấm điểm.
- Nộp bài trên Moodle đúng giờ. Không nhận bài trễ.

# Một số yêu cầu kỹ thuật
1. Ngôn ngữ lập trình Java
2. SV cần viết app Java Swing hoặc Java FX. Các bài làm dạng web app, mobile app sẽ không được chấm điểm.
3. Dữ liệu được lưu trữ dạng text file. Nhớ rằng phải nộp kèm file dữ liệu và phải chứa dữ liệu demo đầy đủ các tính năng.
4. Sử dụng kiến thức về Collection Framework.
5. Sử dụng Git / Github để quản lý source code version. Bài làm cần có ít nhất 5 commits. Số ngày commit phải ít nhất 5 ngày. Mỗi commit chỉ là 1 tính năng. Mỗi tính năng có thể có nhiều commit.
6. Repo cần để mode Private tránh bị mất source code.
7. Nguyên tắc là SV có thể dùng AI hỗ trợ trong quá trình làm bài này. Nhưng: cần sử dụng AI một cách bài bản, ghi nhận lại quá trình sử dụng AI theo file AI Guideline Usage, tự đánh giá được mức độ đóng góp của mình là bao nhiêu, mức độ đóng góp của AI là bao nhiêu trong bài tập, từ đó tìm cách nâng cao chất lượng bài làm, mức độ đóng góp của cá nhân so với SV. Mô tả phần này kỹ trong file tự đánh giá.

# Đề bài
Viết 1 chương trình Java Swing / FX với theo mô tả sau đây.
Thư viện cần quản lý 3 loại thông tin gồm độc giả, sách và các phiếu mượn/trả sách.
1. Thông tin thẻ độc giả cần quản lý bao gồm: mã độc giả, họ tên, CMND, ngày tháng năm sinh, giới tính, email, địa chỉ, ngày lập thẻ và ngày hết hạn của thẻ (48 tháng kể từ ngày lập thẻ).
2. Thông tin sách cần quản lý bao gồm: ISBN (mã sách), tên sách, tác giả, nhà xuất bản, năm xuất bản, thể loại, giá sách, số quyển sách.
3. Mỗi phiếu mượn/trả sách chứa thông tin về mã độc giả, ngày mượn, ngày trả dự kiến, ngày trả thực tế và danh sách ISBN của các sách được mượn. Mỗi sách được mượn tối đa trong 7 ngày, nếu quá hạn sẽ bị phạt tiền 5.000 đồng/ngày. Nếu sách bị mất thì độc giả đó sẽ bị phạt số tiền tương ứng 200% giá sách.
Chương trình có các chức năng sau:
1. Tạo tài khoản thủ thư.
2. Đăng nhập, đăng xuất.
3. Quản lý độc giả.
    1. Xem danh sách độc giả trong thư viện
    1. Thêm độc giả
    1. Chỉnh sửa thông tin một độc giả
    1. Xóa thông tin một độc giả
    1. Tìm kiếm độc giả theo CMND/CCCD
    1. Tìm kiếm độc giả theo họ tên
4. Quản lí sách
    1. Xem danh sách các sách trong thư viện
    1. Thêm sách
    1. Chỉnh sửa thông tin một quyển sách
    1. Xóa thông tin sách
    1. Tìm kiếm sách theo ISBN
    1. Tìm kiếm sách theo tên sách
5. Lập phiếu mượn sách
6. Lập phiếu trả sách
7. Các thống kê cơ bản
    1. Thống kê số lượng sách trong thư viện
    1. Thống kê số lượng sách theo thể loại
    1. Thống kê số lượng độc giả
    1. Thống kê số lượng độc giả theo giới tính
    1. Thống kê số sách đang được mượn
    1. Thống kê danh sách độc giả bị trễ hạn
