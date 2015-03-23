Then /^I open the tools screen$/ do
    Then I press the menu button
    Then I press menu item of translated l10key menu_tools_label
    Then I wait for the "ToolsActivity" to appear
end

Then /^I load the demo data$/ do
    Then I open the tools screen
    Then I press button of translated l10key tools_load_demo_data
    Then I press button "Yes"
    Then I go back
end

Then /^I load the default data$/ do
    Then I open the tools screen
    Then I press button of translated l10key tools_load_default_data
    Then I press button "Yes"
    Then I go back
end