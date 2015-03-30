Feature: Sound

  Free Speech is a picture board, whose most basic function is to say something when a button is pressed.  This should always work.

  Scenario:  I should be able to play a TTS button
    Then I wait up to 5 seconds for the "ViewBoardActivity" screen to appear

    Then I press button of translated l10nkey "fs.i.dont.know.label"
    # TODO:  Define this
    Then I wait for sound to be played
  Scenario:  I should be able to play a cached TTS button
    Then I open the preferences screen
    Then I press toggle button of translated l10nkey "tools_manage_tts_cache"
    Then I go back

    # TODO:  Click the notification bar and confirm that there is a progress message

    Then I wait for 30 seconds
    Then I go back

    Then I press button of translated l10nkey "fs.i.dont.know.label"
    Then I wait for sound to be played

  Scenario:  I should be able to play TTS button once caching is disabled
    Then I open the preferences screen
    Then I press toggle button of translated l10nkey "tools_manage_tts_cache"
    Then I go back

  Scenario:  I should be able to play a recorded sound
    Then I wait up to 5 seconds for the "ViewBoardActivity" screen to appear
    Then I load the demo data
    Then I press the "Recorded Voices" button
    Then I wait for sound to be played
    Then I load the default data
