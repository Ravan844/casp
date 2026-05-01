package com.casp.backend.controller;

import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class AnalyzeController {

    @PostMapping("/analyze")
    public Map<String, Object> analyze(@RequestBody Map<String, String> body) {
        String inputUrl = body.getOrDefault("url", "").trim();
        String normalizedUrl = normalizeUrl(inputUrl);
        String lower = normalizedUrl.toLowerCase();

        int score = 0;
        List<String> issues = new ArrayList<>();

        URI uri;
        try {
            uri = URI.create(normalizedUrl);
        } catch (Exception e) {
            return result(inputUrl, "DANGEROUS", 100, List.of("Invalid or malformed URL"));
        }

        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase();
        String path = uri.getPath() == null ? "" : uri.getPath().toLowerCase();
        String query = uri.getQuery() == null ? "" : uri.getQuery().toLowerCase();

        if (!scheme.equals("https")) {
            score += 25;
            issues.add("Connection is not HTTPS encrypted");
        }

        if (host.isBlank()) {
            score += 40;
            issues.add("URL has no valid domain");
        }

        if (isIpAddress(host)) {
            score += 35;
            issues.add("Uses an IP address instead of a trusted domain");
        }

        if (normalizedUrl.length() > 75) {
            score += 20;
            issues.add("URL is unusually long");
        } else if (normalizedUrl.length() > 45) {
            score += 10;
            issues.add("URL is longer than usual");
        }

        if (host.chars().filter(ch -> ch == '-').count() >= 2) {
            score += 20;
            issues.add("Domain contains multiple hyphens");
        }

        if (countDots(host) >= 4) {
            score += 20;
            issues.add("Too many subdomains");
        }

        if (containsAny(lower, "login", "verify", "password", "reset", "confirm", "account", "secure")) {
            score += 20;
            issues.add("Contains sensitive account/login wording");
        }

        if (containsAny(lower, "free", "gift", "prize", "winner", "urgent", "limited")) {
            score += 15;
            issues.add("Contains social engineering keywords");
        }

        if (isShortener(host)) {
            score += 25;
            issues.add("Uses a URL shortener which hides the final destination");
        }

        if (brandImpersonation(host)) {
            score += 30;
            issues.add("Possible brand impersonation in domain");
        }

        if (query.length() > 40) {
            score += 10;
            issues.add("Long query string may hide tracking or redirect data");
        }

        score = Math.min(score, 100);

        String status = "SAFE";
        if (score >= 70) {
            status = "DANGEROUS";
        } else if (score >= 35) {
            status = "SUSPICIOUS";
        }

        if (issues.isEmpty()) {
            issues.add("No obvious suspicious patterns detected");
            score = 5;
        }

        return result(inputUrl, status, score, issues);
    }

    private String normalizeUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "https://" + url;
        }
        return url;
    }

    private boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word)) return true;
        }
        return false;
    }

    private boolean isIpAddress(String host) {
        return host.matches("\\d+\\.\\d+\\.\\d+\\.\\d+");
    }

    private int countDots(String text) {
        int count = 0;
        for (char c : text.toCharArray()) {
            if (c == '.') count++;
        }
        return count;
    }

    private boolean isShortener(String host) {
        return host.equals("bit.ly")
                || host.equals("tinyurl.com")
                || host.equals("t.co")
                || host.equals("goo.gl")
                || host.equals("ow.ly")
                || host.equals("is.gd");
    }

    private boolean brandImpersonation(String host) {
        List<String> brands = List.of("paypal", "google", "microsoft", "apple", "amazon", "bank");
        List<String> trustedDomains = List.of(
                "paypal.com",
                "google.com",
                "microsoft.com",
                "apple.com",
                "amazon.com"
        );

        for (String trusted : trustedDomains) {
            if (host.equals(trusted) || host.endsWith("." + trusted)) {
                return false;
            }
        }

        for (String brand : brands) {
            if (host.contains(brand)) {
                return true;
            }
        }

        return false;
    }

    private Map<String, Object> result(String url, String status, int score, List<String> issues) {
        Map<String, Object> result = new HashMap<>();
        result.put("url", url);
        result.put("status", status);
        result.put("score", score);
        result.put("issues", issues);
        return result;
    }
}
