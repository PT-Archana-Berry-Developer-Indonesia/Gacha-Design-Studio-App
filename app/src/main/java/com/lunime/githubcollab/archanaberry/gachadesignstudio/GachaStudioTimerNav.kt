/*
package com.lunime.githubcollab.archanaberry.gachadesignstudio;

// TimerTimeout class to manage timeouts for UI visibility
    private class TimerTimeout {
        private val handler = Handler()
        private var timeoutRunnable: Runnable? = null

        fun startTimeout(delayMillis: Long, action: () -> Unit) {
            stopTimeout() // Ensure no other timeout is running
            timeoutRunnable = Runnable {
                action()
            }
            handler.postDelayed(timeoutRunnable, delayMillis)
        }

        fun stopTimeout() {
            timeoutRunnable?.let { handler.removeCallbacks(it) }
        }
    }
}
*/