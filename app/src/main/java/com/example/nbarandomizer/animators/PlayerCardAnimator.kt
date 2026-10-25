package com.example.nbarandomizer.animators

import android.view.animation.OvershootInterpolator
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.example.nbarandomizer.adapters.viewholders.TeamViewHolder

class PlayerCardAnimator : DefaultItemAnimator() {
    private data class Replacement(val old: TeamViewHolder, val new: TeamViewHolder)
    private val replacements = mutableSetOf<Replacement>()

    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder,
        newHolder: RecyclerView.ViewHolder,
        preInfo: ItemHolderInfo,
        postInfo: ItemHolderInfo
    ): Boolean {
        endAnimation(oldHolder)
        endAnimation(newHolder)

        if ((oldHolder as TeamViewHolder).playerId == (newHolder as TeamViewHolder).playerId) {
            oldHolder.itemView.scaleX = 1f
            oldHolder.itemView.scaleY = 1f
            oldHolder.itemView.alpha = 1f

            newHolder.itemView.scaleX = 1f
            newHolder.itemView.scaleY = 1f
            newHolder.itemView.alpha = 1f

            return super.animateChange(oldHolder, newHolder, preInfo, postInfo)
        }

        oldHolder.isAnimating = true
        newHolder.isAnimating = true
        val replacement = Replacement(oldHolder, newHolder)
        replacements.add(replacement)

        val scale = 2f
        newHolder.itemView.alpha = 0f

        oldHolder.itemView.animate()
            .scaleX(scale)
            .scaleY(scale)
            .alpha(0f)
            .setDuration(250)
            .withEndAction {
                if (replacement !in replacements) return@withEndAction
                newHolder.itemView.scaleX = scale
                newHolder.itemView.scaleY = scale

                newHolder.itemView.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .alpha(1f)
                    .setInterpolator(OvershootInterpolator(0.5f))
                    .setDuration(250)
                    .withEndAction {
                        if (replacement !in replacements) return@withEndAction
                        newHolder.itemView.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setInterpolator(OvershootInterpolator(0.5f))
                            .setDuration(150)
                            .withEndAction {
                                finish(replacement)
                            }
                    }
                    .start()
            }
            .start()

        return true
    }

    override fun isRunning() = replacements.isNotEmpty() || super.isRunning()

    override fun endAnimation(item: RecyclerView.ViewHolder) {
        replacements.filter { it.old === item || it.new === item }.forEach(::finish)
        super.endAnimation(item)
    }

    override fun endAnimations() {
        replacements.toList().forEach(::finish)
        super.endAnimations()
    }

    private fun finish(replacement: Replacement) {
        if (!replacements.remove(replacement)) return
        listOf(replacement.old, replacement.new).forEach { holder ->
            holder.itemView.animate().withEndAction(null).cancel()
            holder.itemView.scaleX = 1f
            holder.itemView.scaleY = 1f
            holder.itemView.alpha = 1f
            holder.isAnimating = false
        }
        dispatchChangeFinished(replacement.old, true)
        dispatchChangeFinished(replacement.new, false)
        if (!isRunning) dispatchAnimationsFinished()
    }
}
