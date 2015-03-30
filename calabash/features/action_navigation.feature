Feature: Navigation

  Free Speech is organized into a series of tabs.  It should be possible to navigate between tabs using "action" buttons.

  Scenario:  I should be able to navigate using "action" buttons
    Then I wait up to 5 seconds for the "ViewBoardActivity" screen to appear

    Then I press button of translated l10nkey "fs.i.label"
    Then I wait for the translated "fs.want.to.label" l10nkey to appear


    Then I press button number 1
    Then I wait for the translated "fs.i.label" l10nkey to appear

