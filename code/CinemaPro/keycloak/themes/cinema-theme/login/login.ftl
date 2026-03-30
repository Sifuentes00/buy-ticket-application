<!DOCTYPE html><!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Login</title>
    <link rel="stylesheet" href="${url.resourcesPath}/css/custom-login.css">
</head>
<body>

<div class="container">

    <!-- LEFT (FORM) -->
    <div class="left">
        <div class="card">

            <div class="title">
                Sign in to CinemaPro
            </div>

            <form id="kc-form-login" action="${url.loginAction}" method="post">

                <input
                    type="text"
                    name="username"
                    placeholder="Username or Email"
                    class="input"
                    autofocus
                />

                <input
                    type="password"
                    name="password"
                    placeholder="Password"
                    class="input"
                />

                <button type="submit" class="button">
                    Sign In
                </button>

                <div class="links">
                    <#if realm.resetPasswordAllowed>
                        <a href="${url.loginResetCredentialsUrl}">
                            Forgot password?
                        </a>
                    </#if>

                    <br/>

                    <#if realm.registrationAllowed>
                        <a href="${url.registrationUrl}">
                            Create account
                        </a>
                    </#if>
                </div>

            </form>
        </div>
    </div>

    <!-- RIGHT (VISUAL) -->
    <div class="right">
        <div class="brand">
            🎬 CinemaPro
        </div>
    </div>

</div>

</body>
</html>