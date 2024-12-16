package com.example.app.addon

object GachaStudioErr404 {

    fun webErr(code: Int, fileName: String = "unknown file"): String {
        return if (code == 404) {
            """
            <!DOCTYPE html>
<html>
<head>
  <title>Error Message</title>
  <style>
    body {
      background-color: #000;
      font-family: sans-serif;
      display: flex;
      justify-content: center;
      align-items: center;
      height: 100vh;
    }

    .error-box {
      background-color: #311A28;
      color: #ff7777;
      padding: 20px;
      border-radius: 10px;
      border: 10px solid red;
      box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
    }

    .error-title {
      font-size: 24px;
      font-weight: bold;
      text-align: center;
      margin-bottom: 40px;
    }

    .error-message1 {
      font-size: 18px;
      line-height: 0;
      text-align: center;
    }
    
    .error-message2 {
      font-size: 18px;
      line-height: 1;
      text-align: center;
    }
  
    .error-message3 {
      font-size: 18px;
      line-height: 0;
      text-align: center;
    }
  </style>
</head>
<body>
  <div class="error-box">
    <h2 class="error-title">- ERROR -</h2>
    
    <p class="error-message2">Your device may not be compatible,</p>
    <p class="error-message3">or you do not have enough storage.</p>

    <h2 class="error-title">- ERROR -</h2>
    <p class="error-message1">Unable to run game properly!</p>
    <p class="error-message2">The file <b>"$fileName"</b> could not be found!</p>
    <p class="error-message3">Please check the file path, or reinstall and try again.</p>
  </div>
</body>
</html>
            """.trimIndent()
        } else {
            """
                <!DOCTYPE html>
                <html>
                <head>
                  <title>No Error</title>
                </head>
                <body>
                  <div>
                    <h2>No error. Code: $code</h2>
                  </div>
                </body>
                </html>
            """.trimIndent()
        }
    }
}