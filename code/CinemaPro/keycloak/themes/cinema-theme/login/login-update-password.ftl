<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Update Password</title>
    <link rel="stylesheet" href="${url.resourcesPath}/css/custom-login.css">
</head>
<body>

<div class="container">

    <!-- LEFT -->
    <div class="left">
        <div class="card">

            <div class="title">
                Update your password
            </div>

            <form id="kc-update-password-form"
                  action="${url.loginAction}"
                  method="post">

                <!-- NEW PASSWORD -->
                <input
                        type="password"
                        name="password-new"
                        placeholder="New password"
                        class="input"
                        autofocus
                />

                <#if messagesPerField.existsError('password-new')>
                    <div class="error">
                        ${kcSanitize(messagesPerField.get('password-new'))?no_esc}
                    </div>
                </#if>

                <!-- CONFIRM PASSWORD -->
                <input
                        type="password"
                        name="password-confirm"
                        placeholder="Confirm password"
                        class="input"
                />

                <#if messagesPerField.existsError('password-confirm')>
                    <div class="error">
                        ${kcSanitize(messagesPerField.get('password-confirm'))?no_esc}
                    </div>
                </#if>

                <!-- GLOBAL ERROR -->
                <#if message?has_content>
                    <div class="error">
                        ${kcSanitize(message.summary)?no_esc}
                    </div>
                </#if>

                <!-- SUBMIT -->
                <button type="submit" class="button">
                    Save new password
                </button>

            </form>

        </div>
    </div>

    <!-- RIGHT -->
    <div class="right">
        <div class="brand">
            🎬 Cinema
        </div>
    </div>

</div>

</body>
</html>