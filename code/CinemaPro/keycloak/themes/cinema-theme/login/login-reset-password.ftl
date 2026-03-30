<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Reset Password</title>
    <link rel="stylesheet" href="${url.resourcesPath}/css/custom-login.css">
</head>
<body>

<div class="container">

    <!-- LEFT -->
    <div class="left">
        <div class="card">

            <div class="title">
                Reset password
            </div>

            <form id="kc-reset-password-form" action="${url.loginAction}" method="post">

                <input
                    type="text"
                    name="username"
                    placeholder="Username or Email"
                    class="input"
                    autofocus
                />

                <button type="submit" class="button">
                    Send reset link
                </button>

                <div class="links">
                    <a href="${url.loginUrl}">
                        Back to login
                    </a>
                </div>

            </form>

        </div>
    </div>

    <!-- RIGHT -->
    <div class="right">
        <div class="brand">
            🎬 CinemaPro
        </div>
    </div>

</div>

</body>
</html>
