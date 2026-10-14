package in.mybharat.kavachupi

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {

    private val analyzer = FraudRiskAnalyzer()

    private lateinit var messageInput: EditText
    private lateinit var resultsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootScrollView = ScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#F4F6F9"))
            isFillViewport = true
        }

        val mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val pad = dp(16)
            setPadding(pad, pad, pad, dp(32))
        }

        mainContainer.addView(buildHeaderCard())
        mainContainer.addView(buildDisclaimerBanner(FraudRiskAnalyzer.STANDARD_DISCLAIMER))
        mainContainer.addView(buildInputCard())

        resultsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        mainContainer.addView(resultsContainer)

        mainContainer.addView(buildSafetyTipsCard())

        rootScrollView.addView(mainContainer)
        setContentView(rootScrollView)
    }

    private fun buildHeaderCard(): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }
            background = createRoundedBackground(
                fillColor = Color.parseColor("#0D254C"),
                strokeColor = Color.parseColor("#0D254C"),
                cornerRadiusDp = 14
            )
            val pad = dp(18)
            setPadding(pad, pad, pad, pad)

            val titleView = TextView(this@MainActivity).apply {
                text = "Kavach-UPI"
                setTextColor(Color.WHITE)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
                setTypeface(typeface, Typeface.BOLD)
            }

            val subtitleView = TextView(this@MainActivity).apply {
                text = "Explainable UPI Fraud-Risk Analysis & Digital Payment Safety"
                setTextColor(Color.parseColor("#D0E2FF"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setPadding(0, dp(4), 0, 0)
            }

            val badgeView = TextView(this@MainActivity).apply {
                text = "100% Local Rule-Based Analysis • No Permissions Required"
                setTextColor(Color.parseColor("#A6C8FF"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setPadding(0, dp(8), 0, 0)
            }

            addView(titleView)
            addView(subtitleView)
            addView(badgeView)
        }
    }

    private fun buildDisclaimerBanner(disclaimerText: String): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(14)
            }
            background = createRoundedBackground(
                fillColor = Color.parseColor("#FFF8E6"),
                strokeColor = Color.parseColor("#F0C36D"),
                cornerRadiusDp = 10
            )
            val pad = dp(12)
            setPadding(pad, pad, pad, pad)

            val heading = TextView(this@MainActivity).apply {
                text = "Important Safety Disclaimer"
                setTextColor(Color.parseColor("#7A4F01"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                setTypeface(typeface, Typeface.BOLD)
            }

            val body = TextView(this@MainActivity).apply {
                text = disclaimerText
                setTextColor(Color.parseColor("#5C3C00"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f)
                setPadding(0, dp(4), 0, 0)
            }

            addView(heading)
            addView(body)
        }
    }

    private fun buildInputCard(): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(16)
            }
            background = createRoundedBackground(
                fillColor = Color.WHITE,
                strokeColor = Color.parseColor("#DCE3EC"),
                cornerRadiusDp = 12
            )
            val pad = dp(16)
            setPadding(pad, pad, pad, pad)

            val inputLabel = TextView(this@MainActivity).apply {
                text = "Paste or Type a Payment Message"
                setTextColor(Color.parseColor("#1B263B"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTypeface(typeface, Typeface.BOLD)
            }

            val inputHelper = TextView(this@MainActivity).apply {
                text = "Enter a payment-related SMS or chat message (${FraudRiskAnalyzer.MIN_ANALYZABLE_LENGTH}–${FraudRiskAnalyzer.MAX_INPUT_LENGTH} characters). Never enter your real UPI PIN or OTP."
                setTextColor(Color.parseColor("#5A6978"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f)
                setPadding(0, dp(4), 0, dp(10))
            }

            messageInput = EditText(this@MainActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(12)
                }
                hint = "Example: Approve the collect request and enter your UPI PIN to receive Rs. 2,500 cashback..."
                setHintTextColor(Color.parseColor("#8C9BAE"))
                setTextColor(Color.parseColor("#1B263B"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f)
                minLines = 4
                maxLines = 8
                gravity = Gravity.TOP or Gravity.START
                inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                    InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                filters = arrayOf(InputFilter.LengthFilter(FraudRiskAnalyzer.MAX_INPUT_LENGTH + 50))
                background = createRoundedBackground(
                    fillColor = Color.parseColor("#F8FAFC"),
                    strokeColor = Color.parseColor("#CBD5E1"),
                    cornerRadiusDp = 8
                )
                val fieldPad = dp(12)
                setPadding(fieldPad, fieldPad, fieldPad, fieldPad)
            }

            val buttonRow = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            val analyzeButton = Button(this@MainActivity).apply {
                text = "Analyze Message"
                isAllCaps = false
                setTextColor(Color.WHITE)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                setTypeface(typeface, Typeface.BOLD)
                background = createRoundedBackground(
                    fillColor = Color.parseColor("#1D4ED8"),
                    strokeColor = Color.parseColor("#1D4ED8"),
                    cornerRadiusDp = 8
                )
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1.6f
                ).apply {
                    rightMargin = dp(8)
                }
                setOnClickListener {
                    hideKeyboard()
                    runMessageAnalysis()
                }
            }

            val clearButton = Button(this@MainActivity).apply {
                text = "Clear"
                isAllCaps = false
                setTextColor(Color.parseColor("#1E293B"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                background = createRoundedBackground(
                    fillColor = Color.parseColor("#E2E8F0"),
                    strokeColor = Color.parseColor("#CBD5E1"),
                    cornerRadiusDp = 8
                )
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    0.8f
                )
                setOnClickListener {
                    hideKeyboard()
                    messageInput.setText("")
                    resultsContainer.removeAllViews()
                }
            }

            buttonRow.addView(analyzeButton)
            buttonRow.addView(clearButton)

            addView(inputLabel)
            addView(inputHelper)
            addView(messageInput)
            addView(buttonRow)
        }
    }

    private fun runMessageAnalysis() {
        val rawText = messageInput.text?.toString()
        val result = analyzer.analyze(rawText)
        renderAnalysisResult(result)
    }

    private fun renderAnalysisResult(result: FraudRiskAnalyzer.AnalysisResult) {
        resultsContainer.removeAllViews()

        if (result.validationStatus != FraudRiskAnalyzer.ValidationStatus.VALID) {
            resultsContainer.addView(buildValidationNoticeCard(result))
            return
        }

        resultsContainer.addView(buildSummaryCard(result))
        resultsContainer.addView(buildTriggeredRulesSection(result))
        resultsContainer.addView(buildRecommendedActionsCard(result))
        resultsContainer.addView(buildResultDisclaimerCard(result.disclaimer))
    }

    private fun buildValidationNoticeCard(result: FraudRiskAnalyzer.AnalysisResult): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(16)
            }
            background = createRoundedBackground(
                fillColor = Color.parseColor("#FEF2F2"),
                strokeColor = Color.parseColor("#FCA5A5"),
                cornerRadiusDp = 10
            )
            val pad = dp(14)
            setPadding(pad, pad, pad, pad)

            val statusTitle = TextView(this@MainActivity).apply {
                text = "${result.assessmentSummary.title} (${result.validationStatus.name})"
                setTextColor(Color.parseColor("#991B1B"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                setTypeface(typeface, Typeface.BOLD)
            }

            val messageView = TextView(this@MainActivity).apply {
                text = result.validationMessage ?: result.assessmentSummary.summaryExplanation
                setTextColor(Color.parseColor("#7F1D1D"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f)
                setPadding(0, dp(4), 0, 0)
            }

            addView(statusTitle)
            addView(messageView)
        }
    }

    private fun buildSummaryCard(result: FraudRiskAnalyzer.AnalysisResult): View {
        val summary = result.assessmentSummary
        val palette = getSummaryColors(summary)

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(14)
            }
            background = createRoundedBackground(
                fillColor = palette.backgroundColor,
                strokeColor = palette.borderColor,
                cornerRadiusDp = 12
            )
            val pad = dp(16)
            setPadding(pad, pad, pad, pad)

            val labelView = TextView(this@MainActivity).apply {
                text = "ASSESSMENT SUMMARY"
                setTextColor(palette.titleColor)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f)
                setTypeface(typeface, Typeface.BOLD)
            }

            val titleView = TextView(this@MainActivity).apply {
                text = summary.title
                setTextColor(palette.titleColor)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
                setTypeface(typeface, Typeface.BOLD)
                setPadding(0, dp(4), 0, dp(6))
            }

            val explanationView = TextView(this@MainActivity).apply {
                text = summary.summaryExplanation
                setTextColor(palette.bodyColor)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f)
            }

            val countView = TextView(this@MainActivity).apply {
                text = "Triggered Rules Count: ${result.triggeredRules.size}"
                setTextColor(palette.titleColor)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f)
                setTypeface(typeface, Typeface.BOLD)
                setPadding(0, dp(8), 0, 0)
            }

            addView(labelView)
            addView(titleView)
            addView(explanationView)
            addView(countView)
        }
    }

    private fun buildTriggeredRulesSection(result: FraudRiskAnalyzer.AnalysisResult): View {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(14)
            }
        }

        val sectionHeading = TextView(this).apply {
            text = "Triggered Rules & Explanations"
            setTextColor(Color.parseColor("#1B263B"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, 0, 0, dp(8))
        }
        container.addView(sectionHeading)

        if (result.triggeredRules.isEmpty()) {
            val emptyCard = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                background = createRoundedBackground(
                    fillColor = Color.WHITE,
                    strokeColor = Color.parseColor("#CBD5E1"),
                    cornerRadiusDp = 10
                )
                val pad = dp(14)
                setPadding(pad, pad, pad, pad)

                val emptyTitle = TextView(this@MainActivity).apply {
                    text = "No Specific Risk Rules Triggered"
                    setTextColor(Color.parseColor("#334155"))
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f)
                    setTypeface(typeface, Typeface.BOLD)
                }

                val emptyBody = TextView(this@MainActivity).apply {
                    text = "This message did not match any predefined keyword rules. However, rule-based analysis cannot verify whether a sender is trustworthy or whether a bank transaction actually occurred."
                    setTextColor(Color.parseColor("#475569"))
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                    setPadding(0, dp(4), 0, 0)
                }

                addView(emptyTitle)
                addView(emptyBody)
            }
            container.addView(emptyCard)
            return container
        }

        for (rule in result.triggeredRules) {
            container.addView(buildSingleRuleCard(rule))
        }

        return container
    }

    private fun buildSingleRuleCard(rule: FraudRiskAnalyzer.TriggeredRule): View {
        val severityColors = getSeverityColors(rule.severity)

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(10)
            }
            background = createRoundedBackground(
                fillColor = Color.WHITE,
                strokeColor = severityColors.borderColor,
                cornerRadiusDp = 10
            )
            val pad = dp(14)
            setPadding(pad, pad, pad, pad)

            val ruleIdAndSeverity = TextView(this@MainActivity).apply {
                text = "${rule.ruleId} • ${rule.severity.displayLabel}"
                setTextColor(severityColors.titleColor)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setTypeface(typeface, Typeface.BOLD)
            }

            val ruleTitle = TextView(this@MainActivity).apply {
                text = rule.title
                setTextColor(Color.parseColor("#0F172A"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.5f)
                setTypeface(typeface, Typeface.BOLD)
                setPadding(0, dp(4), 0, dp(6))
            }

            val matchedText = TextView(this@MainActivity).apply {
                val snippets = rule.matchedExcerpts.joinToString(separator = " | ") { "\"$it\"" }
                text = "Matched Pattern(s): $snippets"
                setTextColor(Color.parseColor("#334155"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f)
                setTypeface(typeface, Typeface.ITALIC)
                setPadding(0, 0, 0, dp(8))
            }

            val explanationView = TextView(this@MainActivity).apply {
                text = "Why this triggered: ${rule.explanation}"
                setTextColor(Color.parseColor("#1E293B"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f)
                setPadding(0, 0, 0, dp(8))
            }

            val actionView = TextView(this@MainActivity).apply {
                text = "Recommended Action: ${rule.recommendedAction}"
                setTextColor(severityColors.titleColor)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                setTypeface(typeface, Typeface.BOLD)
            }

            addView(ruleIdAndSeverity)
            addView(ruleTitle)
            addView(matchedText)
            addView(explanationView)
            addView(actionView)
        }
    }

    private fun buildRecommendedActionsCard(result: FraudRiskAnalyzer.AnalysisResult): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(10)
            }
            background = createRoundedBackground(
                fillColor = Color.WHITE,
                strokeColor = severityColors.borderColor,
                cornerRadiusDp = 10
            )
            val pad = dp(14)
            setPadding(pad, pad, pad, pad)

            val ruleIdAndSeverity = TextView(this@MainActivity).apply {
                text = "${rule.ruleId} • ${rule.severity.displayLabel}"
                setTextColor(severityColors.titleColor)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setTypeface(typeface, Typeface.BOLD)
            }

            val ruleTitle = TextView(this@MainActivity).apply {
                text = rule.title
                setTextColor(Color.parseColor("#0F172A"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.5f)
                setTypeface(typeface, Typeface.BOLD)
                setPadding(0, dp(4), 0, dp(6))
            }

            val matchedText = TextView(this@MainActivity).apply {
                val snippets = rule.matchedExcerpts.joinToString(separator = " | ") { "\"$it\"" }
                text = "Matched Pattern(s): $snippets"
                setTextColor(Color.parseColor("#334155"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f)
                setTypeface(typeface, Typeface.ITALIC)
                setPadding(0, 0, 0, dp(8))
            }

            val explanationView = TextView(this@MainActivity).apply {
                text = "Why this triggered: ${rule.explanation}"
                setTextColor(Color.parseColor("#1E293B"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f)
                setPadding(0, 0, 0, dp(8))
            }

            val actionView = TextView(this@MainActivity).apply {
                text = "Recommended Action: ${rule.recommendedAction}"
                setTextColor(severityColors.titleColor)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                setTypeface(typeface, Typeface.BOLD)
            }

            addView(ruleIdAndSeverity)
            addView(ruleTitle)
            addView(matchedText)
            addView(explanationView)
            addView(actionView)
        }
    }

    private fun buildRecommendedActionsCard(result: FraudRiskAnalyzer.AnalysisResult): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(14)
            }
            background = createRoundedBackground(
                fillColor = Color.parseColor("#EFF6FF"),
                strokeColor = Color.parseColor("#93C5FD"),
                cornerRadiusDp = 10
            )
            val pad = dp(14)
            setPadding(pad, pad, pad, pad)

            val heading = TextView(this@MainActivity).apply {
                text = "Recommended Safety Actions"
                setTextColor(Color.parseColor("#1E3A8A"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.5f)
                setTypeface(typeface, Typeface.BOLD)
                setPadding(0, 0, 0, dp(6))
            }
            addView(heading)

            for (action in result.recommendedSafetyActions) {
                val bulletView = TextView(this@MainActivity).apply {
                    text = "• $action"
                    setTextColor(Color.parseColor("#1E40AF"))
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f)
                    setPadding(0, dp(3), 0, dp(3))
                }
                addView(bulletView)
            }
        }
    }

    private fun buildResultDisclaimerCard(disclaimer: String): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(16)
            }
            background = createRoundedBackground(
                fillColor = Color.parseColor("#F1F5F9"),
                strokeColor = Color.parseColor("#CBD5E1"),
                cornerRadiusDp = 8
            )
            val pad = dp(12)
            setPadding(pad, pad, pad, pad)

            val textView = TextView(this@MainActivity).apply {
                text = disclaimer
                setTextColor(Color.parseColor("#475569"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            }
            addView(textView)
        }
    }

    private fun buildSafetyTipsCard(): View {
        val tips = listOf(
            "Never share your UPI PIN, OTP, ATM PIN, or CVV with anyone.",
            "Entering a UPI PIN is required ONLY to send money—never to receive money, refunds, or cashback.",
            "Always verify payment status and account balance directly inside your official UPI or banking app.",
            "Do not trust payment screenshots or plain SMS/chat messages alone as proof of payment.",
            "Be cautious of urgent disconnection threats, unknown links, screen-sharing requests, or .apk files."
        )

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            background = createRoundedBackground(
                fillColor = Color.WHITE,
                strokeColor = Color.parseColor("#DCE3EC"),
                cornerRadiusDp = 12
            )
            val pad = dp(16)
            setPadding(pad, pad, pad, pad)

            val title = TextView(this@MainActivity).apply {
                text = "Digital Payment Safety Tips"
                setTextColor(Color.parseColor("#0F172A"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTypeface(typeface, Typeface.BOLD)
                setPadding(0, 0, 0, dp(8))
            }
            addView(title)

            for (tip in tips) {
                val tipView = TextView(this@MainActivity).apply {
                    text = "✓ $tip"
                    setTextColor(Color.parseColor("#334155"))
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f)
                    setPadding(0, dp(4), 0, dp(4))
                }
                addView(tipView)
            }
        }
    }

    private data class CardColorPalette(
        val backgroundColor: Int,
        val borderColor: Int,
        val titleColor: Int,
        val bodyColor: Int
    )

    private fun getSummaryColors(summary: FraudRiskAnalyzer.AssessmentSummary): CardColorPalette {
        return when (summary) {
            FraudRiskAnalyzer.AssessmentSummary.HIGH_RISK_INDICATORS_DETECTED -> CardColorPalette(
                backgroundColor = Color.parseColor("#FEF2F2"),
                borderColor = Color.parseColor("#EF4444"),
                titleColor = Color.parseColor("#991B1B"),
                bodyColor = Color.parseColor("#7F1D1D")
            )
            FraudRiskAnalyzer.AssessmentSummary.POTENTIAL_RISK_INDICATORS_DETECTED -> CardColorPalette(
                backgroundColor = Color.parseColor("#FFFBEB"),
                borderColor = Color.parseColor("#F59E0B"),
                titleColor = Color.parseColor("#92400E"),
                bodyColor = Color.parseColor("#78350F")
            )
            FraudRiskAnalyzer.AssessmentSummary.UNVERIFIED_PAYMENT_CLAIM_ONLY -> CardColorPalette(
                backgroundColor = Color.parseColor("#F0FDF4"),
                borderColor = Color.parseColor("#22C55E"),
                titleColor = Color.parseColor("#166534"),
                bodyColor = Color.parseColor("#14532D")
            )
            FraudRiskAnalyzer.AssessmentSummary.INSUFFICIENT_INFORMATION -> CardColorPalette(
                backgroundColor = Color.parseColor("#F8FAFC"),
                borderColor = Color.parseColor("#94A3B8"),
                titleColor = Color.parseColor("#1E293B"),
                bodyColor = Color.parseColor("#334155")
            )
            FraudRiskAnalyzer.AssessmentSummary.INVALID_INPUT -> CardColorPalette(
                backgroundColor = Color.parseColor("#FEF2F2"),
                borderColor = Color.parseColor("#FCA5A5"),
                titleColor = Color.parseColor("#991B1B"),
                bodyColor = Color.parseColor("#7F1D1D")
            )
        }
    }

    private fun getSeverityColors(severity: FraudRiskAnalyzer.Severity): CardColorPalette {
        return when (severity) {
            FraudRiskAnalyzer.Severity.HIGH_WARNING -> CardColorPalette(
                backgroundColor = Color.WHITE,
                borderColor = Color.parseColor("#EF4444"),
                titleColor = Color.parseColor("#B91C1C"),
                bodyColor = Color.parseColor("#1E293B")
            )
            FraudRiskAnalyzer.Severity.CAUTION -> CardColorPalette(
                backgroundColor = Color.WHITE,
                borderColor = Color.parseColor("#F59E0B"),
                titleColor = Color.parseColor("#B45309"),
                bodyColor = Color.parseColor("#1E293B")
            )
            FraudRiskAnalyzer.Severity.INFO_VERIFY -> CardColorPalette(
                backgroundColor = Color.WHITE,
                borderColor = Color.parseColor("#0284C7"),
                titleColor = Color.parseColor("#0369A1"),
                bodyColor = Color.parseColor("#1E293B")
            )
        }
    }

    private fun createRoundedBackground(
        fillColor: Int,
        strokeColor: Int,
        cornerRadiusDp: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(fillColor)
            setStroke(dp(1), strokeColor)
            cornerRadius = dp(cornerRadiusDp).toFloat()
        }
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        currentFocus?.let { view ->
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
