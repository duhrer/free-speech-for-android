Feature: Startup

  There are a number of things that have to go right for Free Speech to start up correctly, including any database upgrades, text-to-speech initialization, and of course, loading and displaying the existing data.

  Scenario: I should see the "home" tab when the app is started
    Then I wait up to 5 seconds for the "ViewBoardActivity" screen to appear
    Then I wait for the translated "fs.home.label" l10nkey to appear