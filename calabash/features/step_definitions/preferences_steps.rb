Then /^I open the preferences screen$/ do
    Then I press the menu button
    Then I press menu item of translated l10key menu_preferences_label
    Then I wait for the "PreferencesActivity" to appear
end