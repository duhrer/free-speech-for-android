Feature: Navigation

  Free Speech is organized into a series of tabs.  It should be possible to enable "swipe" navigation and navigate back and forth.

  Scenario: I should be able to use a 1 column layout
    Given I wait 5 seconds for the "ViewBoardActivity" screen to appear

    When I press the menu button
    Then I press menu item of translated l10key menu_preferences_label
    Then I wait for the "PreferencesActivity" to appear
    Then I press text of translated l10key preferences_rows_label
    Then I take a screenshot
    Then I press text "1 column of buttons"
    Then I take a screenshot
    Then I go back
    Then I wait 5 seconds for the "ViewBoardActivity" screen to appear
    Then I wait for the translated "fs.i.label" l10nkey to appear
    Then I take a screenshot

    When I scroll down
    Then I wait for the translated "fs.help.label" l10nkey to appear
    Then I take a screenshot

    When I scroll up
    Then I wait for the translated "fs.i.label" l10nkey to appear
    Then I take a screenshot

  Scenario: I should be able to return to a 3 column layout
    Given I wait 5 seconds for the "ViewBoardActivity" screen to appear

    When I press the menu button
    Then I take a screenshot
    Then I press menu item of translated l10key menu_preferences_label
    Then I wait for the "PreferencesActivity" to appear
    Then I press text of translated l10key preferences_rows_label
    Then I take a screenshot
    Then I press text "3 columns of buttons"
    Then I take a screenshot
    Then I go back
    Then I wait 5 seconds for the "ViewBoardActivity" screen to appear
    Then I take a screenshot

    Then I wait for the translated "fs.i.label" l10nkey to appear
    Then I wait for the translated "fs.help.label" l10nkey to appear
    Then I take a screenshot