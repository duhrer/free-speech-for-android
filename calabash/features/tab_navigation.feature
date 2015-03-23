Feature: Navigation

  Free Speech is organized into a series of tabs.  It should be possible to navigate between tabs using the tab buttons.

  Scenario: I should be able to move back and forth by clicking the tab headings
    Then I wait 5 seconds for the "ViewBoardActivity" screen to appear

    Then I press button of translated l10key fs.actions.label
    Then I wait for the translated "fs.want.to.label" l10nkey to appear
    Then I take a screenshot

    Then I press button of translated l10key fs.home.label
    Then I wait for the translated "fs.i.label" l10nkey to appear
    Then I take a screenshot

