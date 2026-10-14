package in.mybharat.kavachupi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FraudRiskAnalyzerTest {

    private lateinit var analyzer: FraudRiskAnalyzer

    @Before
    fun setUp() {
        analyzer = FraudRiskAnalyzer()
    }

    // 1. Null and empty input
    @Test
    fun analyze_nullAndEmptyInput_returnsEmptyInputValidationAndInvalidSummary() {
        val nullResult = analyzer.analyze(null)
        assertEquals("", nullResult.originalInput)
        assertEquals(FraudRiskAnalyzer.ValidationStatus.EMPTY_INPUT, nullResult.validationStatus)
        assertEquals(FraudRiskAnalyzer.AssessmentSummary.INVALID_INPUT, nullResult.assessmentSummary)
        assertNotNull(nullResult.validationMessage)
        assertTrue(nullResult.triggeredRules.isEmpty())
        assertTrue(nullResult.recommendedSafetyActions.isEmpty())

        val emptyResult = analyzer.analyze("")
        assertEquals("", emptyResult.originalInput)
        assertEquals(FraudRiskAnalyzer.ValidationStatus.EMPTY_INPUT, emptyResult.validationStatus)
        assertEquals(FraudRiskAnalyzer.AssessmentSummary.INVALID_INPUT, emptyResult.assessmentSummary)
        assertNotNull(emptyResult.validationMessage)
        assertTrue(emptyResult.triggeredRules.isEmpty())
        assertTrue(emptyResult.recommendedSafetyActions.isEmpty())
    }

    // 2. Whitespace and short input
    @Test
    fun analyze_whitespaceAndShortInput_returnsExpectedInvalidValidationStatuses() {
        val whitespaceResult = analyzer.analyze("   \t\n   ")
        assertEquals(FraudRiskAnalyzer.ValidationStatus.EMPTY_INPUT, whitespaceResult.validationStatus)
        assertEquals(FraudRiskAnalyzer.AssessmentSummary.INVALID_INPUT, whitespaceResult.assessmentSummary)
        assertNotNull(whitespaceResult.validationMessage)
        assertTrue(whitespaceResult.triggeredRules.isEmpty())

        val shortInput = "Pay"
        assertTrue(shortInput.length < FraudRiskAnalyzer.MIN_ANALYZABLE_LENGTH)
        val shortResult = analyzer.analyze(shortInput)
        assertEquals(FraudRiskAnalyzer.ValidationStatus.TOO_SHORT, shortResult.validationStatus)
        assertEquals(FraudRiskAnalyzer.AssessmentSummary.INVALID_INPUT, shortResult.assessmentSummary)
        assertNotNull(shortResult.validationMessage)
        assertTrue(shortResult.triggeredRules.isEmpty())
    }

    // 3. Input exceeding the maximum length
    @Test
    fun analyze_inputExceedingMaxLength_returnsTooLongValidationStatus() {
        val oversizedInput = "Fictional payment text ".repeat(100)
        assertTrue(oversizedInput.trim().length > FraudRiskAnalyzer.MAX_INPUT_LENGTH)

        val result = analyzer.analyze(oversizedInput)

        assertEquals(FraudRiskAnalyzer.ValidationStatus.TOO_LONG, result.validationStatus)
        assertEquals(FraudRiskAnalyzer.AssessmentSummary.INVALID_INPUT, result.assessmentSummary)
        assertNotNull(result.validationMessage)
        assertTrue(result.triggeredRules.isEmpty())
        assertTrue(result.recommendedSafetyActions.isEmpty())
    }

    // 4. Harmless messages
    @Test
    fun analyze_harmlessEverydayMessage_returnsInsufficientInformationWithNoTriggeredRules() {
        val harmlessMessage =
            "Hello Ananya, let us meet at the college library tomorrow at 4 PM to review our project slides."

        val result = analyzer.analyze(harmlessMessage)

        assertEquals(FraudRiskAnalyzer.ValidationStatus.VALID, result.validationStatus)
        assertNull(result.validationMessage)
        assertEquals(
            FraudRiskAnalyzer.AssessmentSummary.INSUFFICIENT_INFORMATION,
            result.assessmentSummary
        )
        assertTrue(result.triggeredRules.isEmpty())
        assertFalse(result.recommendedSafetyActions.isEmpty())
        assertEquals(FraudRiskAnalyzer.STANDARD_DISCLAIMER, result.disclaimer)
    }

    // 5. Requests for OTP, UPI PIN, or CVV
    @Test
    fun analyze_requestsForOtpUpiPinOrCvv_triggersRuleCred001() {
        val credentialMessage =
            "Fictional Alert: Please share your OTP, enter your UPI PIN, or provide your CVV to verify your account."

        val result = analyzer.analyze(credentialMessage)

        assertEquals(FraudRiskAnalyzer.ValidationStatus.VALID, result.validationStatus)
        assertEquals(
            FraudRiskAnalyzer.AssessmentSummary.HIGH_RISK_INDICATORS_DETECTED,
            result.assessmentSummary
        )
        val rule = result.triggeredRules.find { it.ruleId == "RULE_CRED_001" }
        assertNotNull(rule)
        assertEquals(FraudRiskAnalyzer.Severity.HIGH_WARNING, rule?.severity)
        assertFalse(rule!!.matchedExcerpts.isEmpty())
    }

    // 6. Deceptive PIN-to-receive-money messages
    @Test
    fun analyze_deceptivePinToReceiveMoneyMessage_triggersRulePull002() {
        val deceptivePullMessage =
            "Fictional Reward: Please approve the collect request and input your PIN to receive the cashback into your account."

        val result = analyzer.analyze(deceptivePullMessage)

        assertEquals(FraudRiskAnalyzer.ValidationStatus.VALID, result.validationStatus)
        assertEquals(
            FraudRiskAnalyzer.AssessmentSummary.HIGH_RISK_INDICATORS_DETECTED,
            result.assessmentSummary
        )
        val rule = result.triggeredRules.find { it.ruleId == "RULE_PULL_002" }
        assertNotNull(rule)
        assertEquals(FraudRiskAnalyzer.Severity.HIGH_WARNING, rule?.severity)
        assertFalse(rule!!.matchedExcerpts.isEmpty())
    }

    // 7. Urgent account-blocking threats
    @Test
    fun analyze_urgentAccountBlockingThreat_triggersRuleUrg003() {
        val urgentThreatMessage =
            "Urgent Notice: Your bank account will be blocked today unless you complete verification within 2 hours before it is suspended."

        val result = analyzer.analyze(urgentThreatMessage)

        assertEquals(FraudRiskAnalyzer.ValidationStatus.VALID, result.validationStatus)
        assertEquals(
            FraudRiskAnalyzer.AssessmentSummary.POTENTIAL_RISK_INDICATORS_DETECTED,
            result.assessmentSummary
        )
        val rule = result.triggeredRules.find { it.ruleId == "RULE_URG_003" }
        assertNotNull(rule)
        assertEquals(FraudRiskAnalyzer.Severity.CAUTION, rule?.severity)
        assertFalse(rule!!.matchedExcerpts.isEmpty())
    }

    // 8. Suspicious links
    @Test
    fun analyze_suspiciousLinksInMessage_triggersRuleLink005() {
        val linkMessage =
            "Fictional KYC Notice: Update your payment profile at https://fictional-portal.example/kyc or bit.ly/fictional-kyc-check"

        val result = analyzer.analyze(linkMessage)

        assertEquals(FraudRiskAnalyzer.ValidationStatus.VALID, result.validationStatus)
        assertEquals(
            FraudRiskAnalyzer.AssessmentSummary.POTENTIAL_RISK_INDICATORS_DETECTED,
            result.assessmentSummary
        )
        val rule = result.triggeredRules.find { it.ruleId == "RULE_LINK_005" }
        assertNotNull(rule)
        assertEquals(FraudRiskAnalyzer.Severity.CAUTION, rule?.severity)
        assertTrue(rule!!.matchedExcerpts.any { it.contains("https://") || it.contains("bit.ly/") })
    }

    // 9. Remote-access app references
    @Test
    fun analyze_remoteAccessAppReferences_triggersRuleRemote004() {
        val remoteAccessMessage =
            "Fictional Support: Please open AnyDesk or TeamViewer and enable screen sharing so our agent can check your transaction."

        val result = analyzer.analyze(remoteAccessMessage)

        assertEquals(FraudRiskAnalyzer.ValidationStatus.VALID, result.validationStatus)
        assertEquals(
            FraudRiskAnalyzer.AssessmentSummary.HIGH_RISK_INDICATORS_DETECTED,
            result.assessmentSummary
        )
        val rule = result.triggeredRules.find { it.ruleId == "RULE_REMOTE_004" }
        assertNotNull(rule)
        assertEquals(FraudRiskAnalyzer.Severity.HIGH_WARNING, rule?.severity)
        assertFalse(rule!!.matchedExcerpts.isEmpty())
    }

    // 10. APK installation requests
    @Test
    fun analyze_apkInstallationRequest_triggersRuleRemote004() {
        val apkRequestMessage =
            "Fictional Alert: Please download and install the APK file Wallet_KYC_Update.apk to restore your payment access."

        val result = analyzer.analyze(apkRequestMessage)

        assertEquals(FraudRiskAnalyzer.ValidationStatus.VALID, result.validationStatus)
        assertEquals(
            FraudRiskAnalyzer.AssessmentSummary.HIGH_RISK_INDICATORS_DETECTED,
            result.assessmentSummary
        )
        val rule = result.triggeredRules.find { it.ruleId == "RULE_REMOTE_004" }
        assertNotNull(rule)
        assertEquals(FraudRiskAnalyzer.Severity.HIGH_WARNING, rule?.severity)
        assertTrue(rule!!.matchedExcerpts.any { it.contains(".apk", ignoreCase = true) })
    }

    // 11. Refund pressure
    @Test
    fun analyze_accidentalOverpaymentRefundPressure_triggersRuleRefund006() {
        val refundPressureMessage =
            "Hello, I accidentally sent Rs 4000 instead of Rs 400 to your number by mistake. Please send back the extra money."

        val result = analyzer.analyze(refundPressureMessage)

        assertEquals(FraudRiskAnalyzer.ValidationStatus.VALID, result.validationStatus)
        assertEquals(
            FraudRiskAnalyzer.AssessmentSummary.POTENTIAL_RISK_INDICATORS_DETECTED,
            result.assessmentSummary
        )
        val rule = result.triggeredRules.find { it.ruleId == "RULE_REFUND_006" }
        assertNotNull(rule)
        assertEquals(FraudRiskAnalyzer.Severity.CAUTION, rule?.severity)
        assertFalse(rule!!.matchedExcerpts.isEmpty())
    }

    // 12. Unverified payment claims
    @Test
    fun analyze_unverifiedPaymentClaimOnly_triggersRuleClaim007WithInfoVerifySeverity() {
        val paymentClaimMessage =
            "Fictional Notification: Your account XX4321 has been credited with Rs. 1,500.00 on 14-10-2026."

        val result = analyzer.analyze(paymentClaimMessage)

        assertEquals(FraudRiskAnalyzer.ValidationStatus.VALID, result.validationStatus)
        assertEquals(
            FraudRiskAnalyzer.AssessmentSummary.UNVERIFIED_PAYMENT_CLAIM_ONLY,
            result.assessmentSummary
        )
        assertEquals(1, result.triggeredRules.size)
        val rule = result.triggeredRules.first()
        assertEquals("RULE_CLAIM_007", rule.ruleId)
        assertEquals(FraudRiskAnalyzer.Severity.INFO_VERIFY, rule.severity)
        assertTrue(rule.explanation.contains("does NOT label the message as fraudulent"))
    }

    // 13. Multiple risk indicators
    @Test
    fun analyze_messageWithMultipleRiskIndicators_triggersAllMatchingRulesAndHighRiskSummary() {
        val multiIndicatorMessage =
            "URGENT: Your bank account will be blocked today! Download Support_Helper.apk from https://bit.ly/fictional-update and enter your OTP and UPI PIN to receive your pending refund into your account."

        val result = analyzer.analyze(multiIndicatorMessage)

        assertEquals(FraudRiskAnalyzer.ValidationStatus.VALID, result.validationStatus)
        assertEquals(
            FraudRiskAnalyzer.AssessmentSummary.HIGH_RISK_INDICATORS_DETECTED,
            result.assessmentSummary
        )

        val triggeredIds = result.triggeredRules.map { it.ruleId }.toSet()
        assertTrue(triggeredIds.contains("RULE_CRED_001"))
        assertTrue(triggeredIds.contains("RULE_PULL_002"))
        assertTrue(triggeredIds.contains("RULE_URG_003"))
        assertTrue(triggeredIds.contains("RULE_REMOTE_004"))
        assertTrue(triggeredIds.contains("RULE_LINK_005"))
        assertTrue(result.recommendedSafetyActions.size >= 5)
    }
}
