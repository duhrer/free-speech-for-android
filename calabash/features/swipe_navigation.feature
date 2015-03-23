Feature: Navigation

  Free Speech is organized into a series of tabs.  It should be possible to enable "swipe" navigation and navigate back and forth.

  Scenario: I should be able to enable swiping
    Then I wait 5 seconds for the "ViewBoardActivity" screen to appear

    Then I press the menu button
    Then I take a screenshot
    Then I press menu item of translated l10key menu_preferences_label
    Then I wait for the "PreferencesActivity" to appear
    Then I press toggle button of translated l10key preferences_swipe_tabs_label
    Then I take a screenshot
    Then I go back
    Then I wait 5 seconds for the "ViewBoardActivity" screen to appear

    Then I swipe right
    Then I wait for the translated "fs.want.to.label" l10nkey to appear
    Then I take a screenshot

    Then I swipe left
    Then I wait for the translated "fs.i.label" l10nkey to appear
    Then I take a screenshot


  Scenario: I should be able to disable swiping
    Then I wait 5 seconds for the "ViewBoardActivity" screen to appear

    Then I press the menu button
    Then I press menu item of translated l10key menu_preferences_label
    Then I wait for the "PreferencesActivity" to appear
    Then I press toggle button of translated l10key preferences_swipe_tabs_label
    Then I take a screenshot
    Then I go back
    Then I wait 5 seconds for the "ViewBoardActivity" screen to appear

    Then I swipe right
    Then I wait for the translated "fs.i.label" l10nkey to appear
    Then I take a screenshot

    Then I swipe left
    Then I wait for the translated "fs.i.label" l10nkey to appear
    Then I take a screenshot

