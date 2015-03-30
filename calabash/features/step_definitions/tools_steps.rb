Then /^I open the tools screen$/ do
    macro 'I press the menu key'
    macro 'I press menu item of translated l10nkey "menu_tools_label"'
    macro 'I wait for the "ToolsActivity" to appear'
end

Then /^I load the demo data$/ do
    macro 'I open the tools screen'
    macro 'I press button of translated l10nkey "tools_load_demo_data"'
    macro 'I press button "Yes"'
    macro 'I go back'
end

Then /^I load the default data$/ do
    macro 'I open the tools screen'
    macro 'I press button of translated l10nkey "tools_load_default_data"'
    macro 'I press button "Yes"'
    macro 'I go back'
end