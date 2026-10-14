package in.mybharat.kavachupi

/**
 * Kavach-UPI — Explainable Rule-Based Payment Message Risk Analyzer.
 *
 * IMPORTANT DESIGN CONSTRAINTS:
 * 1. Keyword and pattern matching highlights potential warning indicators for user review;
 *    it NEVER proves that a message is fraudulent or guarantees that a message is safe.
 * 2. This analyzer operates strictly on text provided by the user and CANNOT verify
 *    actual bank balances, UPI ledger statuses, or sender identity.
 * 3. It NEVER requests or processes real UPI PINs, OTPs, or banking credentials.
 */
class FraudRiskAnalyzer {

    companion object {
        const val MIN_ANALYZABLE_LENGTH = 5
        const val MAX_INPUT_LENGTH = 2000

        const val STANDARD_DISCLAIMER =
            "Disclaimer: Kavach-UPI uses local, rule-based text patterns to highlight common " +
                "payment warning signs. Pattern matching cannot verify bank transactions and " +
                "does not guarantee that a message is fraudulent or safe. Always verify payment " +
                "status directly inside your official UPI or banking app."
    }

    enum class ValidationStatus {
        VALID,
        EMPTY_INPUT,
        TOO_SHORT,
        TOO_LONG,
        NO_ANALYZABLE_TEXT
    }

    enum class Severity(val displayLabel: String) {
        HIGH_WARNING("High Warning Indicator"),
        CAUTION("Caution Indicator"),
        INFO_VERIFY("Verification Advisory (Not Automatically Fraud)")
    }

    enum class AssessmentSummary(
        val title: String,
        val summaryExplanation: String
    ) {
        INVALID_INPUT(
            title = "Invalid Input",
            summaryExplanation = "Please enter a valid payment-related message between 5 and 2000 characters."
        ),
        INSUFFICIENT_INFORMATION(
            title = "Insufficient Information / No Specific Rules Triggered",
            summaryExplanation = "No specific UPI risk patterns or payment claims were detected in this text. " +
                "However, the absence of detected keywords does NOT guarantee that a message or transaction is safe."
        ),
        UNVERIFIED_PAYMENT_CLAIM_ONLY(
            title = "Unverified Payment Claim Detected",
            summaryExplanation = "This message contains a claim about a payment or bank credit without overt scam keywords. " +
                "A text message or screenshot is not proof of payment. Verify your balance directly in your official UPI or bank app."
        ),
        POTENTIAL_RISK_INDICATORS_DETECTED(
            title = "Caution Indicators Detected",
            summaryExplanation = "This message contains urgency, external links, or refund pressure patterns commonly seen in payment disputes or social engineering. Proceed with caution."
        ),
        HIGH_RISK_INDICATORS_DETECTED(
            title = "High-Risk Warning Indicators Detected",
            summaryExplanation = "This message contains high-risk indicators such as requests for secret credentials (UPI PIN/OTP), deceptive 'PIN to receive money' claims, or remote-access requests."
        )
    }

    data class TriggeredRule(
        val ruleId: String,
        val title: String,
        val severity: Severity,
        val matchedExcerpts: List<String>,
        val explanation: String,
        val recommendedAction: String
    )

    data class AnalysisResult(
        val originalInput: String,
        val validationStatus: ValidationStatus,
        val validationMessage: String?,
        val assessmentSummary: AssessmentSummary,
        val triggeredRules: List<TriggeredRule>,
        val recommendedSafetyActions: List<String>,
        val disclaimer: String = STANDARD_DISCLAIMER
    )

    private data class RuleDefinition(
        val ruleId: String,
        val title: String,
        val severity: Severity,
        val patterns: List<Regex>,
        val explanation: String,
        val recommendedAction: String
    )

    private val rules: List<RuleDefinition> = listOf(
        // RULE 1: Sensitive Credential Request
        RuleDefinition(
            ruleId = "RULE_CRED_001",
            title = "Request for Sensitive Credentials (UPI PIN / OTP / CVV)",
            severity = Severity.HIGH_WARNING,
            patterns = listOf(
                Regex(
                    pattern = "\\b(share|enter|type|provide|send|verify|input|submit|confirm|tell)\\b[\\s\\S]{0,60}\\b(upi\\s*pin|mpin|atm\\s*pin|otp|one[\\s-]*time\\s*password|cvv|net\\s*banking\\s*password|4[\\s-]*digit\\s*pin|6[\\s-]*digit\\s*pin)\\b",
                    option = RegexOption.IGNORE_CASE
                ),
                Regex(
                    pattern = "\\b(upi\\s*pin|mpin|atm\\s*pin|otp|one[\\s-]*time\\s*password|cvv)\\b[\\s\\S]{0,40}\\b(required|needed|mandatory|to\\s+verify|to\\s+complete)\\b",
                    option = RegexOption.IGNORE_CASE
                )
            ),
            explanation = "The message asks you to enter, share, or verify a secret credential such as a UPI PIN, OTP, ATM PIN, or CVV. Legitimate banks, government offices, and support agents never ask you to share an OTP or enter a UPI PIN for verification.",
            recommendedAction = "Never share your UPI PIN, OTP, ATM PIN, or CVV with anyone, and never enter your UPI PIN on instructions from an unknown caller."
        ),

        // RULE 2: Deceptive "Enter PIN / Scan QR to Receive Money" Claim
        RuleDefinition(
            ruleId = "RULE_PULL_002",
            title = "Deceptive 'Enter PIN or Scan QR to Receive Money' Pattern",
            severity = Severity.HIGH_WARNING,
            patterns = listOf(
                Regex(
                    pattern = "\\b(enter|input|type|use|approve)\\b[\\s\\S]{0,55}\\b(upi\\s*pin|pin|mpin|collect\\s*request)\\b[\\s\\S]{0,55}\\b(to\\s+receive|to\\s+get|to\\s+claim|for\\s+credit|to\\s+accept|into\\s+your\\s+account)\\b",
                    option = RegexOption.IGNORE_CASE
                ),
                Regex(
                    pattern = "\\b(receive|get|claim|credit)\\b[\\s\\S]{0,55}\\b(money|cashback|refund|reward|amount|payment|rs\\.?|inr|₹)\\b[\\s\\S]{0,55}\\b(enter|input|approve|scan)\\b[\\s\\S]{0,40}\\b(upi\\s*pin|pin|qr(?:\\s*code)?|collect\\s*request)\\b",
                    option = RegexOption.IGNORE_CASE
                ),
                Regex(
                    pattern = "\\b(scan\\s+(?:this\\s+|the\\s+)?qr(?:\\s*code)?|approve\\s+(?:the\\s+|this\\s+)?collect\\s*request)\\b[\\s\\S]{0,55}\\b(to\\s+receive|to\\s+get|to\\s+claim|for\\s+refund|for\\s+cashback)\\b",
                    option = RegexOption.IGNORE_CASE
                )
            ),
            explanation = "In UPI, a UPI PIN is ONLY required to authorize money leaving (debiting) your account. You never need to enter a UPI PIN or scan a payment QR code to receive money, refunds, or cashback.",
            recommendedAction = "Decline any UPI Collect Request if you are expecting to receive money. Remember: entering a UPI PIN always deducts money from your account."
        ),

        // RULE 3: Coercive Urgency, Service Disconnection, or Impersonation Threat
        RuleDefinition(
            ruleId = "RULE_URG_003",
            title = "High-Pressure Urgency or Service/Legal Threat",
            severity = Severity.CAUTION,
            patterns = listOf(
                Regex(
                    pattern = "\\b(immediately|urgent(?:ly)?|within\\s+\\d+\\s*(?:hours?|mins?|minutes?)|tonight\\s+at\\s+\\d+)\\b[\\s\\S]{0,65}\\b(block(?:ed)?|suspend(?:ed)?|disconnect(?:ed)?|deactivat(?:ed|e)|froz(?:en|e)|legal\\s+action|arrest|fir|penalty)\\b",
                    option = RegexOption.IGNORE_CASE
                ),
                Regex(
                    pattern = "\\b(electricity|power\\s+supply|bank\\s+account|a/c|kyc|sim\\s+card|pan\\s+card)\\b[\\s\\S]{0,65}\\b(will\\s+be\\s+(?:blocked|suspended|disconnected|deactivated|frozen)|disconnected\\s+tonight|blocked\\s+today|suspended\\s+immediately)\\b",
                    option = RegexOption.IGNORE_CASE
                ),
                Regex(
                    pattern = "\\b(digital\\s+arrest|cbi\\s+warrant|customs\\s+parcel\\s+seized|rbi\\s+escrow\\s+account|police\\s+verification\\s+transfer)\\b",
                    option = RegexOption.IGNORE_CASE
                )
            ),
            explanation = "The message uses artificial time pressure, threats of immediate utility/account disconnection, or law-enforcement intimidation. Scammers frequently create panic so victims act quickly without verifying through official channels.",
            recommendedAction = "Pause and do not panic. Contact your utility provider, bank, or institution only through the official phone number or website printed on your bill or passbook."
        ),

        // RULE 4: Remote Screen-Sharing or Unverified APK Installation Request
        RuleDefinition(
            ruleId = "RULE_REMOTE_004",
            title = "Remote Screen-Sharing or Unverified App (.APK) Request",
            severity = Severity.HIGH_WARNING,
            patterns = listOf(
                Regex(
                    pattern = "\\b(anydesk|teamviewer|rustdesk|quicksupport|airdroid|screen\\s*shar(?:e|ing))\\b",
                    option = RegexOption.IGNORE_CASE
                ),
                Regex(
                    pattern = "\\b[a-zA-Z0-9_\\-]+\\.apk\\b",
                    option = RegexOption.IGNORE_CASE
                ),
                Regex(
                    pattern = "\\b(download|install)\\b[\\s\\S]{0,45}\\b(apk(?:\\s+file)?|verification\\s+app|kyc\\s+app|support\\s+app)\\b",
                    option = RegexOption.IGNORE_CASE
                )
            ),
            explanation = "The message references remote screen-sharing tools or asks you to install an '.apk' file outside the official app store. Screen sharing lets others watch your screen and OTPs in real time, and unknown APK files can forward your SMS messages.",
            recommendedAction = "Never install '.apk' files sent via chat or SMS, and never open your banking or UPI app while screen-sharing or using remote-support apps."
        ),

        // RULE 5: Unverified External or Shortened URL in Payment Context
        RuleDefinition(
            ruleId = "RULE_LINK_005",
            title = "External or Shortened Link in Payment/KYC Message",
            severity = Severity.CAUTION,
            patterns = listOf(
                Regex(
                    pattern = "(https?://[^\\s]+|www\\.[^\\s]+|\\b(?:bit\\.ly|tinyurl\\.com|t\\.co|rb\\.gy|shorturl\\.at|cutt\\.ly)/[^\\s]+)",
                    option = RegexOption.IGNORE_CASE
                )
            ),
            explanation = "The message contains a web link or shortened URL. Unverified links in payment or KYC messages can lead to lookalike phishing pages or trigger unintended payment intents.",
            recommendedAction = "Do not click unknown or shortened links in SMS or chat messages. Open your official UPI or banking application directly from your phone's app drawer."
        ),

        // RULE 6: "Accidental Overpayment" Refund Pressure
        RuleDefinition(
            ruleId = "RULE_REFUND_006",
            title = "Unverified 'Accidental Overpayment' Refund Request",
            severity = Severity.CAUTION,
            patterns = listOf(
                Regex(
                    pattern = "\\b(accidentally|by\\s+mistake|wrongly|mistakenly)\\b[\\s\\S]{0,55}\\b(sent|transferred|paid)\\b[\\s\\S]{0,65}\\b(return|send\\s+back|refund|transfer\\s+back)\\b",
                    option = RegexOption.IGNORE_CASE
                ),
                Regex(
                    pattern = "\\b(sent|transferred)\\b[\\s\\S]{0,40}\\b(instead\\s+of)\\b[\\s\\S]{0,65}\\b(send\\s+back|return|refund|transfer\\s+back)\\b",
                    option = RegexOption.IGNORE_CASE
                )
            ),
            explanation = "The sender claims they accidentally transferred extra money to you and asks you to send it back. This is sometimes paired with a fake SMS or edited screenshot to trick recipients into sending their own money.",
            recommendedAction = "Before returning any claimed overpayment, open your official UPI or bank app and verify your actual account transaction history and updated balance."
        ),

        // RULE 7: Unverified Payment / Credit Notification Claim (Info / Verify — Not Automatically Fraud)
        RuleDefinition(
            ruleId = "RULE_CLAIM_007",
            title = "Unverified Payment or Account Credit Claim",
            severity = Severity.INFO_VERIFY,
            patterns = listOf(
                Regex(
                    pattern = "\\b(credited\\s+with|credited\\s+to\\s+your\\s+(?:a/c|account)|has\\s+been\\s+credited|received\\s+(?:rs\\.?|inr|₹)\\s*[0-9,]+|sent\\s+you\\s+(?:rs\\.?|inr|₹)\\s*[0-9,]+|payment\\s+of\\s+(?:rs\\.?|inr|₹)\\s*[0-9,]+\\s+(?:is\\s+)?successful)\\b",
                    option = RegexOption.IGNORE_CASE
                )
            ),
            explanation = "This message states that money was credited or a payment succeeded. This rule does NOT label the message as fraudulent; however, plain text messages or payment screenshots cannot prove that funds actually settled in your bank account.",
            recommendedAction = "Verify the payment status and actual account balance directly inside your official UPI app or bank passbook before handing over goods or services."
        )
    )

    /**
     * Validates and analyzes a user-supplied message using documented, deterministic rules.
     */
    fun analyze(rawInput: String?): AnalysisResult {
        if (rawInput == null || rawInput.isBlank()) {
            return AnalysisResult(
                originalInput = rawInput ?: "",
                validationStatus = ValidationStatus.EMPTY_INPUT,
                validationMessage = "Message cannot be empty. Please paste or type a payment-related message to analyze.",
                assessmentSummary = AssessmentSummary.INVALID_INPUT,
                triggeredRules = emptyList(),
                recommendedSafetyActions = emptyList()
            )
        }

        val trimmed = rawInput.trim()

        if (trimmed.length < MIN_ANALYZABLE_LENGTH) {
            return AnalysisResult(
                originalInput = rawInput,
                validationStatus = ValidationStatus.TOO_SHORT,
                validationMessage = "Message is too short to analyze (minimum $MIN_ANALYZABLE_LENGTH characters required).",
                assessmentSummary = AssessmentSummary.INVALID_INPUT,
                triggeredRules = emptyList(),
                recommendedSafetyActions = emptyList()
            )
        }

        if (trimmed.length > MAX_INPUT_LENGTH) {
            return AnalysisResult(
                originalInput = rawInput,
                validationStatus = ValidationStatus.TOO_LONG,
                validationMessage = "Message exceeds the maximum allowed length of $MAX_INPUT_LENGTH characters.",
                assessmentSummary = AssessmentSummary.INVALID_INPUT,
                triggeredRules = emptyList(),
                recommendedSafetyActions = emptyList()
            )
        }

        val hasAlphabeticCharacters = trimmed.any { it.isLetter() }
        if (!hasAlphabeticCharacters) {
            return AnalysisResult(
                originalInput = rawInput,
                validationStatus = ValidationStatus.NO_ANALYZABLE_TEXT,
                validationMessage = "Input contains only numbers or symbols. Please enter a message containing words.",
                assessmentSummary = AssessmentSummary.INVALID_INPUT,
                triggeredRules = emptyList(),
                recommendedSafetyActions = emptyList()
            )
        }

        val triggeredRules = mutableListOf<TriggeredRule>()

        for (rule in rules) {
            val matchedSnippets = mutableListOf<String>()
            for (regex in rule.patterns) {
                regex.findAll(trimmed).forEach { matchResult ->
                    val snippet = matchResult.value.trim()
                    if (snippet.isNotEmpty() && !matchedSnippets.contains(snippet)) {
                        matchedSnippets.add(snippet)
                    }
                }
            }

            if (matchedSnippets.isNotEmpty()) {
                triggeredRules.add(
                    TriggeredRule(
                        ruleId = rule.ruleId,
                        title = rule.title,
                        severity = rule.severity,
                        matchedExcerpts = matchedSnippets,
                        explanation = rule.explanation,
                        recommendedAction = rule.recommendedAction
                    )
                )
            }
        }

        val summary = determineAssessmentSummary(triggeredRules)
        val consolidatedActions = buildRecommendedActions(summary, triggeredRules)

        return AnalysisResult(
            originalInput = rawInput,
            validationStatus = ValidationStatus.VALID,
            validationMessage = null,
            assessmentSummary = summary,
            triggeredRules = triggeredRules,
            recommendedSafetyActions = consolidatedActions
        )
    }

    private fun determineAssessmentSummary(triggeredRules: List<TriggeredRule>): AssessmentSummary {
        if (triggeredRules.isEmpty()) {
            return AssessmentSummary.INSUFFICIENT_INFORMATION
        }

        val hasHighWarning = triggeredRules.any { it.severity == Severity.HIGH_WARNING }
        if (hasHighWarning) {
            return AssessmentSummary.HIGH_RISK_INDICATORS_DETECTED
        }

        val hasCaution = triggeredRules.any { it.severity == Severity.CAUTION }
        if (hasCaution) {
            return AssessmentSummary.POTENTIAL_RISK_INDICATORS_DETECTED
        }

        return AssessmentSummary.UNVERIFIED_PAYMENT_CLAIM_ONLY
    }

    private fun buildRecommendedActions(
        summary: AssessmentSummary,
        triggeredRules: List<TriggeredRule>
    ): List<String> {
        val actions = LinkedHashSet<String>()

        triggeredRules.forEach { rule ->
            actions.add(rule.recommendedAction)
        }

        // Always include baseline verification guidance for valid messages
        if (summary == AssessmentSummary.INSUFFICIENT_INFORMATION) {
            actions.add("Even when no warning rules are triggered, never share your UPI PIN or OTP with anyone.")
            actions.add("Always verify any payment status directly inside your official UPI or banking application.")
            actions.add("Do not rely on screenshots or plain text messages alone as proof of payment.")
        } else {
            actions.add("Verify all transaction statuses independently inside your official UPI or banking application.")
        }

        return actions.toList()
    }
}
