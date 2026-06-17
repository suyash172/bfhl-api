package com.example.demo.service.impl;

import com.example.demo.dto.BfhlRequest;
import com.example.demo.dto.BfhlResponse;
import com.example.demo.dto.Summary;
import com.example.demo.service.BfhlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BfhlServiceImpl implements BfhlService {

    private static final Logger log = LoggerFactory.getLogger(BfhlServiceImpl.class);
    private static final Set<Character> VOWELS = Set.of('A','E','I','O','U');

    @Override
    public BfhlResponse process(BfhlRequest request, String requestId) {
        long startTime = System.currentTimeMillis();
        log.info("Processing started: requestId={}", requestId);

        List<String> rawData = request.getData();
        int totalReceived = rawData.size();

        // Step 1: Filter invalid (null, empty, whitespace)
        List<String> validData = rawData.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .collect(Collectors.toList());

        int invalidIgnored = totalReceived - validData.size();

        // Step 2: Detect duplicates before dedup
        Set<String> seen = new HashSet<>();
        boolean containsDuplicates = false;
        for (String s : validData) {
            if (!seen.add(s)) {
                containsDuplicates = true;
                break;
            }
        }

        // Step 3: Deduplicate (preserve order)
        List<String> dedupedData = validData.stream()
                .distinct()
                .collect(Collectors.toList());

        int uniqueElementCount = dedupedData.size();

        // Step 4: Classify each element
        List<BigDecimal> numbers = new ArrayList<>();
        List<String> alphabetStrings = new ArrayList<>(); // whole alpha strings like "ABC"
        List<String> alphabetChars = new ArrayList<>();   // individual letters for count/freq
        List<String> specialCharacters = new ArrayList<>();

        for (String element : dedupedData) {
            if (isNumeric(element)) {
                numbers.add(new BigDecimal(element));
            } else if (isAlpha(element)) {
                // whole alpha string like "ABC" or "A"
                alphabetStrings.add(element.toUpperCase());
            } else if (isAlphanumeric(element)) {
                // extract digits
                String digitPart = element.replaceAll("[^0-9]", "");
                String alphaPart = element.replaceAll("[^a-zA-Z]", "");
                if (!digitPart.isEmpty()) {
                    try { numbers.add(new BigDecimal(digitPart)); } catch (Exception ignored) {}
                }
                if (!alphaPart.isEmpty()) {
                    for (char c : alphaPart.toUpperCase().toCharArray()) {
                        alphabetChars.add(String.valueOf(c));
                    }
                }
            } else {
                specialCharacters.add(element);
            }
        }
        // Step 5: Build alphabets list for response
        // From example 5: response shows both whole strings and individual extracted letters
        // Response alphabets: pure alpha strings + individual letters from alphanumeric
        List<String> responseAlphabets = new ArrayList<>(alphabetStrings);
        responseAlphabets.addAll(alphabetChars);
        // Step 6: Number computations
        List<String> oddNumbers = new ArrayList<>();
        List<String> evenNumbers = new ArrayList<>();
        BigDecimal sum = BigDecimal.ZERO;

        for (BigDecimal num : numbers) {
            sum = sum.add(num);
            // Only classify integers as odd/even
            if (num.scale() <= 0 || num.stripTrailingZeros().scale() <= 0) {
                long longVal = num.longValue();
                if (longVal % 2 == 0) {
                    evenNumbers.add(num.stripTrailingZeros().toPlainString());
                } else {
                    oddNumbers.add(num.stripTrailingZeros().toPlainString());
                }
            } else {
                // Decimal numbers go to even by convention (not odd)
                evenNumbers.add(num.stripTrailingZeros().toPlainString());
            }
        }

        // Sorted numbers
        List<BigDecimal> sortedNums = new ArrayList<>(numbers);
        Collections.sort(sortedNums);
        List<String> sortedNumbers = sortedNums.stream()
                .map(n -> n.stripTrailingZeros().toPlainString())
                .collect(Collectors.toList());

        // Largest and smallest
        String largestNumber = numbers.isEmpty() ? null :
                Collections.max(numbers).stripTrailingZeros().toPlainString();
        String smallestNumber = numbers.isEmpty() ? null :
                Collections.min(numbers).stripTrailingZeros().toPlainString();

        // Sum string
        String sumStr = sum.stripTrailingZeros().toPlainString();

        // Step 7: Alphabet frequency and vowel count
        // Combine alphabetStrings chars + alphabetChars for frequency
        // allLetters = individual chars from pure alpha strings + extracted chars from alphanumeric
        List<String> allLetters = new ArrayList<>();
        for (String s : alphabetStrings) {
            for (char c : s.toCharArray()) {
                allLetters.add(String.valueOf(c));
            }
        }
// alphabetChars already has individual letters from alphanumeric strings
        allLetters.addAll(alphabetChars);

// alphabet_count = total individual letters

        Map<String, Integer> alphabetFrequency = new LinkedHashMap<>();
        int vowelCount = 0;
        int consonantCount = 0;
        for (String letter : allLetters) {
            char c = letter.charAt(0);
            alphabetFrequency.merge(String.valueOf(c), 1, Integer::sum);
            if (VOWELS.contains(c)) vowelCount++;
            else consonantCount++;
        }

        // Step 8: Longest and shortest alphabetic string
        String longestAlphabeticValue = alphabetStrings.isEmpty() ? null :
                alphabetStrings.stream().max(Comparator.comparingInt(String::length)).orElse(null);
        String shortestAlphabeticValue = alphabetStrings.isEmpty() ? null :
                alphabetStrings.stream().min(Comparator.comparingInt(String::length)).orElse(null);

        // Step 9: Counts
        int alphabetCount = allLetters.size();
        int numberCount = numbers.size();
        int specialCharacterCount = specialCharacters.size();

        // Step 10: Summary
        Summary summary = new Summary(totalReceived, uniqueElementCount, invalidIgnored);

        long processingTime = System.currentTimeMillis() - startTime;
        log.info("Processing complete: requestId={}, time={}ms", requestId, processingTime);

        // Step 11: Build response
        BfhlResponse response = new BfhlResponse();
        response.setSuccess(true);
        response.setRequestId(requestId);
        response.setOddNumbers(oddNumbers);
        response.setEvenNumbers(evenNumbers);
        response.setAlphabets(responseAlphabets);
        response.setSpecialCharacters(specialCharacters);
        response.setSum(sumStr);
        response.setLargestNumber(largestNumber);
        response.setSmallestNumber(smallestNumber);
        response.setAlphabetCount(alphabetCount);
        response.setNumberCount(numberCount);
        response.setSpecialCharacterCount(specialCharacterCount);
        response.setContainsDuplicates(containsDuplicates);
        if (containsDuplicates) {
            response.setUniqueElementCount(uniqueElementCount);
        }
        response.setSortedNumbers(sortedNumbers);
        response.setVowelCount(vowelCount);
        response.setConsonantCount(consonantCount);
        response.setAlphabetFrequency(alphabetFrequency);
        response.setLongestAlphabeticValue(longestAlphabeticValue);
        response.setShortestAlphabeticValue(shortestAlphabeticValue);
        response.setProcessingTimeMs(processingTime);
        response.setSummary(summary);

        return response;
    }

    private boolean isNumeric(String s) {
        try {
            new BigDecimal(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isAlpha(String s) {
        return s.matches("[a-zA-Z]+");
    }

    private boolean isAlphanumeric(String s) {
        return s.matches("[a-zA-Z0-9.\\-]+") && s.matches(".*[a-zA-Z].*") && s.matches(".*[0-9].*");
    }
}