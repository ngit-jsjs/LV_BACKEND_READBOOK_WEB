package org.example.lv_backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.lv_backend.entity.Book;
import org.example.lv_backend.entity.Chapter;
import org.example.lv_backend.mapper.BookMapper;
import org.example.lv_backend.repository.BookRepository;
import org.example.lv_backend.repository.ChapterRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EpubParserService {
    private final ChapterRepository chapterRepository;

    // ============================
    // 1. IMPORT CHƯƠNG TỪ EPUB
    // ============================
    @Transactional
    protected List<Chapter> parseAndSaveChapters(Book book) {
        List<Chapter> chapters = new ArrayList<>();
        try {
            EpubReader epubReader = new EpubReader();
            nl.siegmann.epublib.domain.Book epubBook;
            try (FileInputStream fis = new FileInputStream(book.getStoragePath())) {
                epubBook = epubReader.readEpub(fis);
            }

            // Xây dựng bản đồ: href file → vị trí trong Spine (sectionIndex)
            Map<String, Integer> hrefToSpineIndex = new HashMap<>();
            List<SpineReference> spineRefs = epubBook.getSpine().getSpineReferences();
            for (int i = 0; i < spineRefs.size(); i++) {
                Resource res = spineRefs.get(i).getResource();
                if (res != null) {
                    hrefToSpineIndex.put(res.getHref(), i);
                }
            }

            // Duyệt đệ quy theo MỤC LỤC (TOC) thay vì Spine
            List<TOCReference> flatToc = new ArrayList<>();
            flattenToc(epubBook.getTableOfContents().getTocReferences(), flatToc);

            int chapterNumber = 1;
            for (int i = 0; i < flatToc.size(); i++) {
                TOCReference tocRef = flatToc.get(i);
                if (tocRef.getResource() == null) continue;

                String href = tocRef.getResource().getHref();
                String title = tocRef.getTitle();
                String fragmentId = tocRef.getFragmentId(); // ví dụ: "chap2"

                // Tìm sectionIndex (vị trí trong Spine) dựa trên href
                Integer sectionIndex = hrefToSpineIndex.get(href);
                if (sectionIndex == null) continue;

                // Tìm anchor của chương TIẾP THEO (nếu cùng file HTML)
                String nextAnchor = null;
                if (i + 1 < flatToc.size()) {
                    TOCReference nextToc = flatToc.get(i + 1);
                    if (nextToc.getResource() != null
                        && nextToc.getResource().getHref().equals(href)) {
                        // Chương tiếp theo nằm CÙNG file HTML → lưu anchor
                        nextAnchor = nextToc.getFragmentId();
                    }
                }

                // Nếu tiêu đề rỗng thì đặt tên mặc định
                if (title == null || title.isBlank()) {
                    title = "Chương " + chapterNumber;
                }

                Chapter chapter = Chapter.builder()
                        .book(book)
                        .chapterNumber(chapterNumber)
                        .sectionIndex(sectionIndex)
                        .title(title.trim())
                        .fragmentId(fragmentId)
                        .nextAnchor(nextAnchor)
                        .isFree(false)
                        .price(BigDecimal.valueOf(10))
                        .isPublished(false)
                        .build();
                chapters.add(chapter);
                chapterNumber++;
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi parse file EPUB", e);
        }
        return chapterRepository.saveAll(chapters);
    }

    private void flattenToc(List<TOCReference> refs, List<TOCReference> result) {
        if (refs == null) return;
        for (TOCReference ref : refs) {
            result.add(ref);
            if (ref.getChildren() != null && !ref.getChildren().isEmpty()) {
                flattenToc(ref.getChildren(), result);
            }
        }
    }

    // ============================
    // 2. ĐỌC NỘI DUNG CHƯƠNG
    // ============================
    public String readChapterContent(String epubPath, Integer sectionIndex,
                                     String fragmentId, String nextAnchor) {
        try {
            EpubReader epubReader = new EpubReader();
            nl.siegmann.epublib.domain.Book epubBook;
            try (FileInputStream fis = new FileInputStream(epubPath)) {
                epubBook = epubReader.readEpub(fis);
            }
            List<SpineReference> spineRefs = epubBook.getSpine().getSpineReferences();

            if (sectionIndex < 0 || sectionIndex >= spineRefs.size()) {
                return "";
            }

            Resource resource = spineRefs.get(sectionIndex).getResource();
            String rawHtml;
            try (InputStream is = resource.getInputStream()) {
                rawHtml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }

            // 1. Đọc và trích xuất CSS từ file HTML GỐC (trước khi bị cắt mất phần <head>)
            Document originalDoc = Jsoup.parse(rawHtml);
            List<String> cssContents = new ArrayList<>();
            for (Element link : originalDoc.select("link[rel=stylesheet], link[type=text/css]")) {
                String href = link.attr("href");
                if (href == null || href.isBlank()) {
                    continue;
                }

                // Giải quyết đường dẫn tương đối của CSS trong EPUB
                String resolvedHref = resolveRelativePath(resource.getHref(), href);

                // Lấy file CSS từ EPUB resources
                Resource cssResource = epubBook.getResources().getByHref(resolvedHref);
                if (cssResource != null) {
                    try (InputStream cssIs = cssResource.getInputStream()) {
                        String cssContent = new String(cssIs.readAllBytes(), StandardCharsets.UTF_8);
                        cssContents.add(cssContent);
                    } catch (Exception ignored) {
                    }
                }
            }

            // 2. Cắt HTML theo fragmentId và nextAnchor để lấy đúng nội dung chương
            String slicedHtml = sliceHtmlByAnchors(rawHtml, fragmentId, nextAnchor);
            Document doc = Jsoup.parse(slicedHtml);

            // 3. Nhúng các đoạn CSS đã trích xuất vào đầu body của tài liệu đã cắt
            if (doc.body() != null) {
                for (String cssContent : cssContents) {
                    Element styleEl = doc.createElement("style");
                    styleEl.text(cssContent);
                    doc.body().prependChild(styleEl);
                }
            }

            // 4. Nhúng ảnh trực tiếp vào HTML dưới dạng Base64 để hiển thị
            for (Element img : doc.select("img")) {
                String src = img.attr("src");
                if (src == null || src.isBlank() || src.startsWith("data:")) {
                    continue;
                }

                // Giải quyết đường dẫn tương đối của ảnh trong EPUB
                String resolvedHref = resolveRelativePath(resource.getHref(), src);

                // Lấy file ảnh từ EPUB resources
                Resource imgResource = epubBook.getResources().getByHref(resolvedHref);
                if (imgResource != null) {
                    try (InputStream imgIs = imgResource.getInputStream()) {
                        byte[] imgBytes = imgIs.readAllBytes();
                        String base64Data = java.util.Base64.getEncoder().encodeToString(imgBytes);
                        String mimeType = imgResource.getMediaType().getName();

                        // Cập nhật thẻ img src bằng Base64 data URI
                        img.attr("src", "data:" + mimeType + ";base64," + base64Data);
                    } catch (Exception ignored) {
                    }
                }
            }

            String finalHtml = doc.body() != null ? doc.body().html() : doc.html();
            try {
                java.nio.file.Files.writeString(
                    java.nio.file.Paths.get("d:\\Files\\ALUANVANTOTNGHIEP\\LV_BACKEND\\lv_backend\\uploads\\temp_chapter.html"),
                    finalHtml
                );
            } catch (Exception ignored) {}
            return finalHtml;

        } catch (Exception e) {
            throw new RuntimeException("Không đọc được nội dung chapter từ EPUB", e);
        }
    }

    // ============================
    // 3. CẮT HTML THEO ANCHOR
    // ============================
    // Ví dụ: file HTML chứa Chương 1 (id="chap1"), Chương 2 (id="chap2"), Chương 3 (id="chap3")
    // Gọi sliceHtmlByAnchors(html, "chap2", "chap3")
    // → Trả về HTML chỉ từ phần tử id="chap2" cho đến trước id="chap3"
    private String sliceHtmlByAnchors(String html, String startAnchor, String endAnchor) {
        try {
            Document doc = Jsoup.parse(html);

            // Tìm phần tử bắt đầu bằng id hoặc name
            Element startEl = null;
            if (startAnchor != null && !startAnchor.isBlank()) {
                startEl = doc.getElementById(startAnchor);
                if (startEl == null) {
                    startEl = doc.selectFirst("[name=\"" + startAnchor + "\"]");
                }
            }

            // Tìm phần tử kết thúc (anchor của chương tiếp theo)
            Element endEl = null;
            if (endAnchor != null && !endAnchor.isBlank()) {
                endEl = doc.getElementById(endAnchor);
                if (endEl == null) {
                    endEl = doc.selectFirst("[name=\"" + endAnchor + "\"]");
                }
            }

            // Nếu không có startAnchor hoặc không tìm thấy startEl
            if (startEl == null) {
                // Nếu startAnchor rỗng/null, tức là chương bắt đầu từ đầu file
                if (startAnchor == null || startAnchor.isBlank()) {
                    Element body = doc.body();
                    if (body != null && !body.children().isEmpty()) {
                        startEl = body.child(0);
                    } else {
                        startEl = body;
                    }
                }
            }

            // Nếu vẫn null (không tìm thấy cả startAnchor lẫn không có phần tử nào trong body)
            if (startEl == null) {
                return html;
            }

            // Thu thập nội dung: từ startEl cho đến endEl (hoặc hết file)
            StringBuilder sb = new StringBuilder();

            // Nếu startEl chính là endEl (đề phòng)
            if (endEl != null && startEl.equals(endEl)) {
                return "";
            }

            sb.append(startEl.outerHtml());

            // Duyệt các phần tử anh em (sibling) sau startEl
            Element current = startEl.nextElementSibling();
            while (current != null) {
                // Nếu gặp phần tử kết thúc → dừng lại
                if (endEl != null && current.equals(endEl)) {
                    break;
                }
                // Kiểm tra xem phần tử con có chứa endEl không (dùng phương thức an toàn thay vì bộ chọn CSS thô dễ lỗi)
                if (endEl != null && (current.getElementById(endAnchor) != null || !current.select("[name=\"" + endAnchor + "\"]").isEmpty())) {
                    break;
                }
                sb.append(current.outerHtml());
                current = current.nextElementSibling();
            }
            return sb.toString();

        } catch (Exception e) {
            return html; // Nếu lỗi → trả về nguyên bản
        }
    }

    // ============================
    // 4. GIẢI QUYẾT ĐƯỜNG DẪN TƯƠNG ĐỐI
    // ============================
    private String resolveRelativePath(String basePath, String relativePath) {
        if (basePath == null || relativePath == null) return relativePath;

        if (relativePath.startsWith("/") || relativePath.contains("://") || relativePath.startsWith("data:")) {
            return relativePath;
        }

        String[] baseParts = basePath.split("/");
        List<String> pathList = new ArrayList<>();
        for (int i = 0; i < baseParts.length - 1; i++) {
            pathList.add(baseParts[i]);
        }

        String[] relParts = relativePath.split("/");
        for (String part : relParts) {
            if (part.equals("..")) {
                if (!pathList.isEmpty()) {
                    pathList.remove(pathList.size() - 1);
                }
            } else if (!part.equals(".") && !part.isEmpty()) {
                pathList.add(part);
            }
        }

        return String.join("/", pathList);
    }
}
