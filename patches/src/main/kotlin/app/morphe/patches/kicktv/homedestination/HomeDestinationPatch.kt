package app.morphe.patches.kicktv.homedestination

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.patch
import app.morphe.patches.kicktv.KICK_TV
import app.morphe.patches.kicktv.settings.settingsPatch

private const val EXTENSION_CLASS = "Lapp/morphe/extension/kicktv/homedestination/"

/** MainActivity.onResume() - app becomes visible, after MorpheContext.setActivity() has run. */
private object MainActivityOnResumeFingerprint : Fingerprint(
    definingClass = "Lcom/kick/tv/MainActivity;",
    name = "onResume",
    parameters = emptyList(),
    returnType = "V",
)

/**
 * DEFAULT HOME DESTINATION tweak. On app open, if the user picked a destination
 * other than the stock Home tab, KICK navigates there automatically. The choice
 * lives in SEYTTINGS and survives restarts. Requires the SEYTTINGS patch.
 *
 * <p>The navigation target method must be derived from KICK 1.9.0 with jadx
 * (see AGENTS.md §5). The skeleton below injects the preference read into
 * {@code MainActivity.onCreate()}; the actual tab-switch call goes in
 * {@code HomeDestinationHelper.navigateTo(destination)} once the KICK navigation
 * API is known.</p>
 */
@Suppress("unused")
val homeDestinationPatch = patch(
    name = "DEFAULT HOME DESTINATION",
    description = "Opens the chosen tab (Home, Categories, Live, Following or " +
        "Settings) automatically when KICK starts. Pick it in SEYTTINGS. " +
        "Requires the SEYTTINGS patch.",
    default = true,
) {
    compatibleWith(KICK_TV)

    dependsOn(settingsPatch)

    extendWith("extensions/KickHomeDestination.mpe")

    execute {
        MainActivityOnResumeFingerprint.method.addInstructions(
            0,
            """
                invoke-static {p0}, ${EXTENSION_CLASS}HomeDestinationHelper;->onAppOpen(Landroid/app/Activity;)V
            """,
        )
    }
}
