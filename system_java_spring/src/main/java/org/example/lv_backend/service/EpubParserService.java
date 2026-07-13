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
import org.example.lv_backend.exception.AppException;
import org.example.lv_backend.exception.ErrorCode;

import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
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


    @Transactional
    protected List<Chapter> parseAndSaveChapters(Book book) {
        List<Chapter> chapters = new ArrayList<>();
        try {
            EpubReader epubReader = new EpubReader();
            nl.siegmann.epublib.domain.Book epubBook;
            try (FileInputStream fis = new FileInputStream(book.getStoragePath())) {
                epubBook = epubReader.readEpub(fis);
            }

            Map<String, Integer> hrefToSpineIndex = new HashMap<>();
            List<SpineReference> spineRefs = epubBook.getSpine().getSpineReferences();
            for (int i = 0; i < spineRefs.size(); i++) {
                Resource res = spineRefs.get(i).getResource();
                if (res != null) {
                    hrefToSpineIndex.put(res.getHref(), i);
                }
            }

            List<TOCReference> flatToc = new ArrayList<>();
            flattenToc(epubBook.getTableOfContents().getTocReferences(), flatToc);

            int chapterNumber = 1;
            for (int i = 0; i < flatToc.size(); i++) {
                TOCReference tocRef = flatToc.get(i);
                if (tocRef.getResource() == null) continue;

                String href = tocRef.getResource().getHref();
                String title = tocRef.getTitle();
                String fragmentId = tocRef.getFragmentId(); 

                Integer sectionIndex = hrefToSpineIndex.get(href);
                if (sectionIndex == null) continue;

                String nextAnchor = null;
                if (i + 1 < flatToc.size()) {
                    TOCReference nextToc = flatToc.get(i + 1);
                    if (nextToc.getResource() != null
                        && nextToc.getResource().getHref().equals(href)) {
                        nextAnchor = nextToc.getFragmentId();
                    }
                }

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
                        .build();
                chapters.add(chapter);
                chapterNumber++;
            }
        } catch (Exception e) {
            throw new AppException(ErrorCode.EPUB_PARSE_FAILED);
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

    public String readChapterContent(String epubPath, Integer sectionIndex,
                                     String fragmentId, String nextAnchor) {
        try {
            EpubReader epubReader = new EpubReader();
            nl.siegmann.epublib.domain.Book epubBook;
            try (FileInputStream fis = new FileInputStream(epubPath)) {
                epubBook = epubReader.readEpub(fis);
            }
            List<SpineReference> spineRefs = epubBook.getSpine().getSpineReferences();

            // if (sectionIndex < 0 || sectionIndex >= spineRefs.size()) {
            //     return "";
            // }

            Resource resource = spineRefs.get(sectionIndex).getResource();
            String rawHtml;
            try (InputStream is = resource.getInputStream()) {
                rawHtml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }

            List<String> cssContents = new ArrayList<>();
            for (Resource res : epubBook.getResources().getAll()) {
                if (res.getHref() != null && res.getHref().toLowerCase().endsWith(".css")) {
                    try (InputStream cssIs = res.getInputStream()) {
                        String cssContent = new String(cssIs.readAllBytes(), StandardCharsets.UTF_8);
                        cssContents.add(cssContent);
                    } catch (Exception ignored) {
                    }
                }
            }

            String slicedHtml = sliceHtmlByAnchors(rawHtml, fragmentId, nextAnchor);
            Document doc = Jsoup.parse(slicedHtml);

            if (doc.body() != null) {
                for (String cssContent : cssContents) {
                    Element styleEl = doc.createElement("style");
                    styleEl.text(cssContent);
                    doc.body().prependChild(styleEl);
                }
            }

            for (Element img : doc.select("img")) {
                String src = img.attr("src");
                if (src == null || src.isBlank() || src.startsWith("data:")) {
                    continue;
                }

                String resolvedHref = resolveRelativePath(resource.getHref(), src);

                Resource imgResource = epubBook.getResources().getByHref(resolvedHref);
                if (imgResource != null) {
                    try (InputStream imgIs = imgResource.getInputStream()) {
                        byte[] imgBytes = imgIs.readAllBytes();
                        String base64Data = java.util.Base64.getEncoder().encodeToString(imgBytes);
                        String mimeType = imgResource.getMediaType().getName();

                        img.attr("src", "data:" + mimeType + ";base64," + base64Data);
                    } catch (Exception ignored) {
                    }
                }
            }

            return doc.body() != null ? doc.body().html() : doc.html();

        } catch (Exception e) {
            throw new AppException(ErrorCode.EPUB_CHAPTER_CONTENT_READ_FAILED);
        }
    }

 
    private String sliceHtmlByAnchors(String html, String startAnchor, String endAnchor) {
        try {
            Document doc = Jsoup.parse(html);

            Element startEl = null;
            if (startAnchor != null && !startAnchor.isBlank()) {
                startEl = doc.getElementById(startAnchor);
                if (startEl == null) {
                    startEl = doc.selectFirst("[name=\"" + startAnchor + "\"]");
                }
            }

            Element endEl = null;
            if (endAnchor != null && !endAnchor.isBlank()) {
                endEl = doc.getElementById(endAnchor);
                if (endEl == null) {
                    endEl = doc.selectFirst("[name=\"" + endAnchor + "\"]");
                }
            }

            if (startEl == null) {
                if (startAnchor == null || startAnchor.isBlank()) {
                    Element body = doc.body();
                    if (body != null && !body.children().isEmpty()) {
                        startEl = body.child(0);
                    } else {
                        startEl = body;
                    }
                }
            }


            StringBuilder sb = new StringBuilder();

            if (endEl != null && startEl.equals(endEl)) {
                return "";
            }

            sb.append(startEl.outerHtml());

            Element current = startEl.nextElementSibling();
            while (current != null) {
                if (endEl != null && current.equals(endEl)) {
                    break;
                }
                if (endEl != null && (current.getElementById(endAnchor) != null || !current.select("[name=\"" + endAnchor + "\"]").isEmpty())) {
                    break;
                }
                sb.append(current.outerHtml());
                current = current.nextElementSibling();
            }
            return sb.toString();

        } catch (Exception e) {
            return html; 
        }
    }


    private String resolveRelativePath(String basePath, String relativePath) {
        if (basePath == null || relativePath == null) return relativePath;

        if (relativePath.startsWith("/") || relativePath.contains("://") || relativePath.startsWith("data:")) {
            return relativePath;
        }

        return URI.create(basePath).resolve(relativePath).toString();
    }
}
