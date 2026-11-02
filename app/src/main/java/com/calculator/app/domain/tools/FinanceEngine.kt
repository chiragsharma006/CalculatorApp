package com.calculator.app.domain.tools

import kotlin.math.pow

data class LoanEmiResult(
    val monthlyEmi: Double,
    val totalInterest: Double,
    val totalPayment: Double,
    val principalRatio: Float,
    val interestRatio: Float
)

data class SipResult(
    val totalInvested: Double,
    val estimatedReturns: Double,
    val totalValue: Double,
    val investedRatio: Float,
    val returnsRatio: Float
)

data class GstResult(
    val netAmount: Double,
    val gstAmount: Double,
    val cgstAmount: Double,
    val sgstAmount: Double,
    val grossAmount: Double
)

data class DiscountResult(
    val originalPrice: Double,
    val finalPrice: Double,
    val totalSavings: Double,
    val effectiveDiscountPercent: Double
)

object FinanceEngine {

    fun calculateLoanEmi(
        principal: Double,
        annualRatePercent: Double,
        tenureMonths: Int
    ): LoanEmiResult {
        if (principal <= 0 || tenureMonths <= 0) {
            return LoanEmiResult(0.0, 0.0, 0.0, 1f, 0f)
        }

        if (annualRatePercent <= 0.0) {
            val emi = principal / tenureMonths
            return LoanEmiResult(
                monthlyEmi = emi,
                totalInterest = 0.0,
                totalPayment = principal,
                principalRatio = 1f,
                interestRatio = 0f
            )
        }

        val monthlyRate = (annualRatePercent / 12.0) / 100.0
        val factor = (1.0 + monthlyRate).pow(tenureMonths.toDouble())
        val emi = principal * monthlyRate * factor / (factor - 1.0)
        val totalPayment = emi * tenureMonths
        val totalInterest = totalPayment - principal

        val principalRatio = if (totalPayment > 0) (principal / totalPayment).toFloat() else 1f
        val interestRatio = 1f - principalRatio

        return LoanEmiResult(
            monthlyEmi = emi,
            totalInterest = totalInterest,
            totalPayment = totalPayment,
            principalRatio = principalRatio,
            interestRatio = interestRatio
        )
    }

    fun calculateSip(
        monthlyInvestment: Double,
        annualRatePercent: Double,
        years: Double
    ): SipResult {
        val months = (years * 12).toInt()
        if (monthlyInvestment <= 0 || months <= 0) {
            return SipResult(0.0, 0.0, 0.0, 1f, 0f)
        }

        val totalInvested = monthlyInvestment * months

        if (annualRatePercent <= 0.0) {
            return SipResult(
                totalInvested = totalInvested,
                estimatedReturns = 0.0,
                totalValue = totalInvested,
                investedRatio = 1f,
                returnsRatio = 0f
            )
        }

        val monthlyRate = (annualRatePercent / 12.0) / 100.0
        // FV = P * [ (1 + i)^n - 1 ] * (1 + i) / i
        val factor = (1.0 + monthlyRate).pow(months.toDouble())
        val futureValue = monthlyInvestment * (factor - 1.0) * (1.0 + monthlyRate) / monthlyRate
        val returns = futureValue - totalInvested

        val investedRatio = if (futureValue > 0) (totalInvested / futureValue).toFloat() else 1f
        val returnsRatio = 1f - investedRatio

        return SipResult(
            totalInvested = totalInvested,
            estimatedReturns = returns,
            totalValue = futureValue,
            investedRatio = investedRatio,
            returnsRatio = returnsRatio
        )
    }

    fun calculateLumpsum(
        totalInvestment: Double,
        annualRatePercent: Double,
        years: Double
    ): SipResult {
        if (totalInvestment <= 0 || years <= 0) {
            return SipResult(totalInvestment, 0.0, totalInvestment, 1f, 0f)
        }

        val futureValue = totalInvestment * (1.0 + (annualRatePercent / 100.0)).pow(years)
        val returns = futureValue - totalInvestment
        val investedRatio = if (futureValue > 0) (totalInvestment / futureValue).toFloat() else 1f
        val returnsRatio = 1f - investedRatio

        return SipResult(
            totalInvested = totalInvestment,
            estimatedReturns = returns,
            totalValue = futureValue,
            investedRatio = investedRatio,
            returnsRatio = returnsRatio
        )
    }

    fun calculateGst(
        amount: Double,
        gstRatePercent: Double,
        isAddGst: Boolean
    ): GstResult {
        if (amount <= 0 || gstRatePercent <= 0) {
            return GstResult(amount, 0.0, 0.0, 0.0, amount)
        }

        return if (isAddGst) {
            // Net to Gross: Add GST
            val gstAmount = amount * (gstRatePercent / 100.0)
            val grossAmount = amount + gstAmount
            GstResult(
                netAmount = amount,
                gstAmount = gstAmount,
                cgstAmount = gstAmount / 2.0,
                sgstAmount = gstAmount / 2.0,
                grossAmount = grossAmount
            )
        } else {
            // Gross to Net: Remove GST
            val netAmount = (amount * 100.0) / (100.0 + gstRatePercent)
            val gstAmount = amount - netAmount
            GstResult(
                netAmount = netAmount,
                gstAmount = gstAmount,
                cgstAmount = gstAmount / 2.0,
                sgstAmount = gstAmount / 2.0,
                grossAmount = amount
            )
        }
    }

    fun calculateDiscount(
        originalPrice: Double,
        discountPercent: Double,
        extraDiscountPercent: Double = 0.0
    ): DiscountResult {
        if (originalPrice <= 0) {
            return DiscountResult(0.0, 0.0, 0.0, 0.0)
        }

        val step1Price = originalPrice * (1.0 - (discountPercent / 100.0).coerceIn(0.0, 1.0))
        val finalPrice = step1Price * (1.0 - (extraDiscountPercent / 100.0).coerceIn(0.0, 1.0))
        val totalSavings = originalPrice - finalPrice
        val effectivePercent = if (originalPrice > 0) (totalSavings / originalPrice) * 100.0 else 0.0

        return DiscountResult(
            originalPrice = originalPrice,
            finalPrice = finalPrice,
            totalSavings = totalSavings,
            effectiveDiscountPercent = effectivePercent
        )
    }
}
