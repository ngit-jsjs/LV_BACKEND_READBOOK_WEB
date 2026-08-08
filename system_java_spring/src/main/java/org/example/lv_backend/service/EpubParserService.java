package org.example.lv_backend.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.lv_backend.entity.Book;
import org.example.lv_backend.entity.Chapter;
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

    private static class PendingPart {
        Integer sectionIndex;
        String fragmentId;
        String nextAnchor;
        String title;

        PendingPart(Integer sectionIndex, String fragmentId, String nextAnchor, String title) {
            this.sectionIndex = sectionIndex;
            this.fragmentId = fragmentId;
            this.nextAnchor = nextAnchor;
            this.title = title;
        }
    }

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
            PendingPart pendingPart = null;

            for (int i = 0; i < flatToc.size(); i++) {
                TOCReference tocRef = flatToc.get(i);
                if (tocRef.getResource() == null) continue;

                String href = tocRef.getResource().getHref();
                String title = tocRef.getTitle();
                String fragmentId = tocRef.getFragmentId();

                if (isIgnoredMetadataSection(title, href)) {
                    continue;
                }

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

                if (isPartDivider(tocRef, title)) {
                    if (pendingPart == null) {
                        pendingPart = new PendingPart(sectionIndex, fragmentId, nextAnchor, title.trim());
                    } else {
                        pendingPart.title = pendingPart.title + " - " + title.trim();
                    }
                    continue;
                }

                String finalTitle = title.trim();
                Integer partSecIdx = null;
                String partFragId = null;
                String partNextAnc = null;

                if (pendingPart != null) {
                    partSecIdx = pendingPart.sectionIndex;
                    partFragId = pendingPart.fragmentId;
                    partNextAnc = pendingPart.nextAnchor;

                    if (!finalTitle.toLowerCase().contains(pendingPart.title.toLowerCase())) {
                        finalTitle = pendingPart.title + " - " + finalTitle;
                    }

                    pendingPart = null;
                }

                Chapter chapter = Chapter.builder()
                        .book(book)
                        .chapterNumber(chapterNumber)
                        .sectionIndex(sectionIndex)
                        .title(finalTitle)
                        .fragmentId(fragmentId)
                        .nextAnchor(nextAnchor)
                        .partSectionIndex(partSecIdx)
                        .partFragmentId(partFragId)
                        .partNextAnchor(partNextAnc)
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

    private boolean isIgnoredMetadataSection(String title, String href) {
        String t = (title != null) ? title.toLowerCase().trim() : "";
        String h = (href != null) ? href.toLowerCase().trim() : "";

        if (t.equals("cover") || t.equals("bìa") || t.equals("bìa sách")
                || t.contains("title page") || t.contains("trang tên sách") || t.contains("titlepage")
                || t.contains("copyright") || t.contains("bản quyền")
                || t.contains("table of contents") || t.equals("contents") || t.equals("mục lục") || t.equals("toc")
                || t.contains("dedication") || t.contains("lời đề tặng")
                || t.contains("publisher") || t.contains("oceanofpdf")) {
            return true;
        }

        if (h.contains("cover") || h.contains("titlepage") || h.contains("copyright")
                || h.contains("toc.xhtml") || h.contains("nav.xhtml")) {
            return true;
        }

        return false;
    }

    private boolean isPartDivider(TOCReference tocRef, String title) {
        if (title == null) return false;
        String t = title.toLowerCase().trim();

        boolean titleMatchesPart = t.matches("^(part|phần|book|volume|quyển)\\s+([ivxlcdm\\d]+|one|two|three|four|five|six|seven|eight|nine|ten).*")
                || t.equals("part") || t.equals("phần") || t.equals("book");

        if (!titleMatchesPart) {
            return false;
        }

        try {
            Resource res = tocRef.getResource();
            if (res != null) {
                String rawHtml = new String(res.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                Document doc = Jsoup.parse(rawHtml);
                String text = doc.text().trim();
                return text.length() < 600;
            }
        } catch (Exception ignored) {
        }

        return true;
    }

    private void flattenToc(List<TOCReference> refs, List<TOCReference> result) {
        if (refs == null) return;
        for (TOCReference ref : refs) {
            TOCReference firstChild = null;
            if (ref.getChildren() != null && !ref.getChildren().isEmpty()) {
                firstChild = ref.getChildren().get(0);
            }

            boolean shouldMerge = false;
            if (firstChild != null && ref.getResource() != null && firstChild.getResource() != null) {
                String refHref = ref.getResource().getHref();
                String childHref = firstChild.getResource().getHref();
                if (refHref != null && refHref.equals(childHref)) {
                    shouldMerge = true;
                }
            }

            if (shouldMerge) {
                String parentTitle = ref.getTitle() != null ? ref.getTitle().trim() : "";
                String childTitle = firstChild.getTitle() != null ? firstChild.getTitle().trim() : "";
                if (!parentTitle.equalsIgnoreCase(childTitle)) {
                    ref.setTitle(parentTitle + " - " + childTitle);
                }
                result.add(ref);
                
                List<TOCReference> remainingChildren = new java.util.ArrayList<>(ref.getChildren());
                remainingChildren.remove(0);
                flattenToc(remainingChildren, result);
            } else {
                result.add(ref);
                if (ref.getChildren() != null && !ref.getChildren().isEmpty()) {
                    flattenToc(ref.getChildren(), result);
                }
            }
        }
    }

    public String readChapterContent(String epubPath, Integer sectionIndex,
                                     String fragmentId, String nextAnchor,
                                     Integer partSectionIndex, String partFragmentId, String partNextAnchor) {
        try {
            EpubReader epubReader = new EpubReader();
            nl.siegmann.epublib.domain.Book epubBook;
            try (FileInputStream fis = new FileInputStream(epubPath)) {
                epubBook = epubReader.readEpub(fis);
            }

            String mainContent = readSingleSectionHtml(epubBook, sectionIndex, fragmentId, nextAnchor);

            if (partSectionIndex != null) {
                String partContent = readSingleSectionHtml(epubBook, partSectionIndex, partFragmentId, partNextAnchor);
                if (partContent != null && !partContent.isBlank()) {
                    return partContent + "<hr class=\"part-divider\" style=\"margin: 30px 0; border: none; border-top: 1px solid rgba(255,255,255,0.15);\" />" + mainContent;
                }
            }

            return mainContent;
        } catch (Exception e) {
            throw new AppException(ErrorCode.EPUB_CHAPTER_CONTENT_READ_FAILED);
        }
    }

    public String readChapterContent(String epubPath, Integer sectionIndex,
                                     String fragmentId, String nextAnchor) {
        return readChapterContent(epubPath, sectionIndex, fragmentId, nextAnchor, null, null, null);
    }

    private String readSingleSectionHtml(nl.siegmann.epublib.domain.Book epubBook, Integer sectionIndex,
                                         String fragmentId, String nextAnchor) {
        if (sectionIndex == null) return "";
        List<SpineReference> spineRefs = epubBook.getSpine().getSpineReferences();
        if (sectionIndex < 0 || sectionIndex >= spineRefs.size()) {
            return "";
        }

        Resource resource = spineRefs.get(sectionIndex).getResource();
        if (resource == null) return "";

        String rawHtml;
        try (InputStream is = resource.getInputStream()) {
            rawHtml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
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
