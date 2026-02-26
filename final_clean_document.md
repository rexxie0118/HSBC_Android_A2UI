# Rendering Engine Design and Specification

Original document compiled from images o1-o12

Note: Sample renderer and parser documentation is available at SmartForms Engine Documentation.

1. Overview

The rendering engine takes a parsed form definition (see json_parser_spec.md ) and renders it as an interactive multi-view form. The engine must:

- Render multiple views with navigation

* Support 19 question component types

- Evaluate rules client-side (visibility, read-only, value, choices, validation)

- Submit data back to ODS via PUT/POST/PATCH endpoints

- Handle the USER and TASK context objects for rule evaluation

2. Architecture

Three layers:

1. Form Engine -- manages state: values, visibility, validations, metadata, actions. Evaluates all rules.

2. Renderer -- maps each item type to a native Ul component. Consumes state from the engine.

3. Host Integration -- handles API calls (GET/PUT/POST/PATCH) and context (USER, TASK objects).

The engine and renderer are decoupled. The engine produces a state object, the renderer reads it.

too - oa === + 4o-------------- ===: + $o--------------------+

| Host Integration| ----> | Form Engine |--> | Renderer |

| (API, Context) | | (State, Rules) | | (Native UI) |

+ +

| GET/PUT/POST/PATCH | state object | user events

| USER, TASK context | (values, visibility, | (input, tap,

| | errors, choices, etc.) | navigate)

v v v

ODS Backend Rule Evaluation Platform UI Kit
3. State Management

The engine maintains the following state:

State Key Type Description

values Map<questionId, any> Current answer for each question

visibility Map<itemId, boolean> Computed visibility for each item

readOnly Map<itemId, boolean> Computed read-only state

errors Map<itemId, string[]> Validation error messages

choices Map<questionId, ChoiceItem[]> | Computed choice lists

currentViewId string The active view

completedViews Set<string> Views that have been completed

3.1 State Update Cycle

When a user changes an answer:

1, Update values [questionId]

2. Find all items whose rules depend on this questionld (use *Dependencies arrays)

3. Re-evaluate affected visibility rules, readOnly rules, value rules, choices rules, validation rules

4. Update renderer state

User Input

|

v

values [questionId] = newValue

i

v

Lookup dependents via *Dependencies arrays

i

+---> Re-evaluate visibleRule for affected items

-> Re-evaluate readOnlyRule for affected items

-> Re-evaluate valueRule for affected items

-> Re-evaluate choicesRule for affected items

-> Re-evaluate validation rules for affected items

Renderer receives updated state

3.2 Dependency Tracking

Each rule has a corresponding dependencies array that lists the question IDs it depends on. Use these arrays to build a reverse index:

dependencyIndex: Map<questionId, Set<itemId>>

When questionId changes, look up all items in dependencyIndex[questionId] and re-evaluate their rules.
4. Rule Evaluation

See rule_spec.md for detailed expression evaluation. The engine must implement the following rule types:

4.1 Visibility Rules (visibleRule)

- Present on items and views

- Evaluates to truthy/falsy

- When false, item is hidden and its value is excluded from submission (unless includeInRulesEvaluationIfHidden=true )

- Dependencies listed in visibleRuleDependencies

if evaluate(item.visibleRule, context) is falsy:

visibility[item.id] = false

exclude values[item.id] from submission (unless includeInRulesEvaluationIfHidden)

else:

visibility[item.id] = true

4.2 Read-Only Rules (readOnlyRule)

- Present in props

- Evaluates to truthy/falsy

- When true, input is disabled/non-editable

- Dependencies in readOnlyRuleDependencies

if evaluate(item.props.readOnlyRule, context) is truthy:

readOnly[item.id] = true

render component as disabled

else:

readOnly[item.id] = false

4.3 Value Rules (valueRule)

- Presentin props

- Evaluates to a computed value that overrides the user's input

- Used for auto-calculated fields

- Dependencies in valueRuleDependencies

computedValue = evaluate(item.props.valueRule, context)

if computedValue is not null:

values [item.id] = computedValue

render component with computedValue
4.4 Choices Rules (choicesRule)

e- Present in options

- Evaluates to an array of {value, text} objects

e Replaces static choices with dynamic ones

e Individual choices may also have disableRule and visibleRule

dynamicChoices = evaluate(item.props.options.choicesRule, context)

if dynamicChoices is not null:

choices[item.id] = dynamicChoices

else:

choices[item.id] = item.props.options.choices // static fallback

// For each choice, evaluate per-choice rules:

for choice in choices[item. id]:

if choice.disableRule:

choice.disabled = evaluate(choice.disableRule, context)

if choice.visibleRule:

choice.visible = evaluate(choice.visibleRule, context)
4.5 Validation Rules

Array of validationItem objects on items or in props . Four types:

Type Rule Meaning

OPTIONAL Rule evaluates to truthy = field is optional (not required)

CUSTOM Rule evaluates to truthy = validation passes

MAX_LENGTH rule contains the maximum character length (e.g. "255" )

REGEXP rule contains the regex pattern (e.g. "*[A-Za-z]+$" )

Validation runs on navigate (PUT) and submit (POST):

for each validationItem in item.validations:

switch validationItem. type:

case OPTIONAL:

isOptional = evaluate(validationItem.rule, context)

if not isOptional and isEmpty(values[item. id]):

errors [item. id] .push(validationItem.message)

case CUSTOM:

passes = evaluate(validationItem.rule, context)

if not passes:

errors [item. id] .push(validationItem.message)

case MAX_LENGTH:

maxLen = parseInt(validationItem. rule)

if length(values[item.id]) > maxLen:

errors [item. id] .push(validationItem.message)

case REGEXP:

pattern = validationItem.rule // rule IS the regex pattern

if not matches(values[item.id], pattern):

errors [item. id] .push(validationItem.message)

4.6 Next View ID Rule (nextViewldRule)

* Present on views and action buttons

- Evaluates to a view ID string

* Determines which view to navigate to after Continue

nextViewId = evaluate(view.nextViewIdRule, context)

if nextViewId is not null:

navigate to view with id = nextViewId

else:

navigate to next visible view in linear order

Error Condition

Value is empty when rule is falsy (field is required)

Rule evaluates to falsy

Value length exceeds the limit in rule

Value does not match the pattern in rule
5. View Navigation

5.1 Ordering

* Views are ordered inthe views array

* Default navigation: linear order ( view[@] -> view[1] ->...-> viewI[n] )

* nextViewIdRule overrides default navigation (e.g., skip views based on answers)

- hideFromMenu views are not shown in the left navigation menu

5.2 Navigation Flow

On navigate (Continue button):

1. Run validations for current view

2. Evaluate nextViewIdRule to determine the target view (or default to next visible view in linear order)

3. If valid, call PUT /v2/smart-forms/{odsId} with { currentViewId, data, nextViewId }

4. On success, navigate to the view indicated by the response's currentViewld

5. Render the new view

Important: The server does NOT evaluate nextViewIdRule - it relies on the client to send the computed nextViewld in the PUT request body. If nextViewId is omitted, the server defaults to the next view in linear order. Therefore, the

client must evaluate nextViewIdRule BEFORE calling PUT.

User taps "Continue"

v

Run validations for all visible items in current view

> Errors found?

-> Display inline errors, block navigation

v (no errors)

Evaluate nextViewIdRule

+---> Rule returns viewId? ---> targetViewId = result

v (no rule or null result)

targetViewId = next visible view in linear order

v

PUT /v2/smart-forms/{odsId} with { currentViewId, data, nextViewld: targetViewId }

+---> API error? --> Display error message

v (success)

Mark currentViewId as completed

Navigate to response's currentViewld

v

Render new view
6. Component Type Rendering

6.1 SECTION

Renders a heading/divider that groups subsequent items.

Props: text, type

- Display as a styled heading

- May be collapsible if nested in GROUP with accordion option

6.2 TEXT

Renders read-only informational text.

Props: text , style, notificationType , listStyleType

Prop Values

text string or markdown

style "s4" others

notificationType "warning", "information" , “error”

listStyleType string

evalText true / false

Behavior

Plain string or markdown (supports [text] (url) , *kbold**, "r"n )

"s4" for body text, other styles for headings

Renders as a styled notification banner

Renders text as a list with the given style

When true, text contains expressions to evaluate dynamically
6.3 QUESTION Subtypes

cach QUESTION renders citfereny based on props. type

props.type LUtcompenent Value Type

1NeuT_Text Text tole string

‘TEXTAREA Multiine wtarea string

ADTO_BUTTON ago button group string

SELECT_ONE orepdoun string

SELECT May Muit-select ropdown -stringL

cvecK 0x checkboxes)

{INEUT_NUMER Numeric text ld

{NPUT_HONEY currency + amount object

INPUT_DATE ate picker string

INPUT_DATE MANY --Repeatable date picker string(]

JINPUT_PHONE NUMBER, Country code + number object

INPUT MANY Repeatabe text input string]

FILE_UPLOAD File picker object 1

‘ones ‘Adress form object

TABLE Editable able object

‘TABLE_SEARCH Searchabie table object

‘TABLE_GREENSTAR Specialized table object {1

srOWATURES Signature collection object. [1

“TIME_RANGE Timerangeseector object

Component Rendering Details

INPUT_TEXT

tater: (tooltip) |

eee

| | placenotaer cont

eee

| Error message (1t any)

RADIO_SUTTON

Label (eoottip) |

) option &

1

i

om

1 G9 option 8

1 1) Option -

i

1

1

i

i

1

1

Error message (if any) 1

SELECT_ONE/ SELECT_MANY

| Laer (toot) |

[i srs ERG)

| | Setectea vatuets) M1

| Error message (11 any)

INPUT_MONEY

(eeottip) |

a ee

[| uso Mw | | eee i

eel

| Error massage (if any)

TABLE

| Later (tootti) |

[jae eee eer reees

i

TL COLA| CoLB | cole | Action} |

{pee Bee i

Liu fv fa | |

Fjwu jv jae | ol |

|p es ee A)

| As Row 1

| Error message (1f any)

string o steing{]

runber / string

Notes

Single-tn

options.max for char mit

Single value from choices

Single valve from choices

Array of selected values

Single or multiple

‘options.decinalscale , thousandSeparator

{amount, currency} or simitar

180 date string ( YYYY-HN-00 )

Muttiole dates

{countryCode, phonetuster}

‘Adalremove items

((fAtetiome, fiLevutd)]

{addressLinel, city, countryCode, postalcode,

‘Array of tow objects, colurms from options. view

‘Selected raw trom cholees

Variant of TABLE

‘Signatory names and status

from, toy
6.4 ACTIONS

Renders a button ba.

Property

type

text

visibleRute

nextViewidRute

pdtContig

payleadconfig

subait

validationMode

visibleRuleDependencies

nextViewIdRuledependencies

Each action button has:

Type Description

string CONTINUE, SAVE, SUBMIT, BACK, CANCEL, REVIEW, APPROVE , REJECT, EDIT

string Button label

expression Show/hide the button

expression Override navigation target

object POF generation configuration

array ‘Configuration for button payload

boolean ‘Whether this button triggers form submission

string Validation mode for this button

array of string

array of sting

‘On click: dispatch corresponding API cal

Dependencies tor visible rule

Dependencies for next-view rule

Behavior

‘Action Type API Call

courte PUT /v2/snart-forns/{odsTd}

SAVE PATCH /v2/ssart-forns /{odsTd)

suewrt POST /v2/snart-forns/{odstd)

Back None

concen one

Review PUT /v2/snart-forns/{odsTé}

‘APPROVE POST /v2/snart-forms/{odsId)

REJECT POST /v2/smart-forns/{odstd)

err None

DISCARD None

DOWRLOAD_POF None

6.5 GROUP

Renders a collapsiblejexpandable container with nested items.

+ props-options. view. itens contains the nested items

+ props. options-accordion=true

+ props. options.collapse=true ~- start collapsed

* Used for guidance notes, definitions, expandable sections

1

+

i)

MW

i

+1

6.6 LEFT_NAV_MENU

Save current view navigate to next

Save current data without navigating

Submit the entire form

Navigate to previous view

atthe form

Navigate to review/summary view

Submit with reviewState: "APPROVED" via payloadContig

Submit with reviewState: "REJECTED" via payloadcontig

Switch to elt mode forthe curent view

Discard unsaved changes

Generate and download POF ofthe form

render as accordion

Renders the left sidebar navigation, Liss all visible views with completion indicators.

| View 2

| View 2

| View 3

| view 4

+ Only show views where hideFronMenu is not rue

‘Highlight me active view ( currentViewTd )

+ Show completion indicator for views in completedViews

‘Tapping a completed view navigates back tot

6.7 DETAILS

Renders key-value pairs for review/summary screens.

| Later Vatwe a 1

| Labet 2: Value 2 1

| Lapet 3: Value 3 1

DISCARD , DOWNLOAD_POF
6.8 GROUP_BUTTONS

Renders action buttons within a group context. Behaves like ACTIONS but scoped to the parent GROUP.

6.9 Reference Items (refltem)

© Identified by refItemId field (no type field)

* Resolve by looking up the referenced item from the item index

* Apply any props overrides from the refitem onto the resolved item

* Apply visibleRule override if present

Resolution process:

1. Find item where refItemId matches an existing item's ID

2. Deep-clone the referenced item

3. Merge refltem.props over cloned item.props (shallow merge)

4. If refltem.visibleRule exists, override cloned item.visibleRule

5. Render the resolved item as usual

7. Text Rendering

* Text props may contain markdown: [link text] (url) , **bold**, "r"n_ for newlines

* When evalText=true , the text value is an expression that must be evaluated before display

* Text can be a plain string or an object with locale keys for i18n

7.1 Markdown Support

The following markdown features must be supported in text rendering:

Syntax Rendering

[text] (url) Clickable hyperlink

wxbold textex Bold text

"r"n or "n Line break

Inline expressions (when evalText=true ) Evaluated and substituted

7.2 Internationalization (i18n)

Select the text matching the current user locale, falling back to "en" if the locale is not available.

8. Tooltips

* Many items havea tooltip prop

* Display as an info icon that shows the tooltip content on tap/hover

* When evalTooltip=true , tooltip content is an expression to evaluate

=

| Labet @ 4

I I i

I

|

|

&

ttt |

| Tooltip text | |

------+ |
9. Data Submission Format

When calling PUTIPOST/PATCH, the data| object is a flat map of question ID to answer value

«

‘currentViewTa": “section_1",

data": {

"SeQ7Radio": "Yes

"countrySelect":

*aateFiela”

“nunberFiet

9.1 Submission Rules

* Only include visible questions in submission (unless._includeTnRuLesEvaluationIfHidden=true )

* Value rules produce computed values that must also be included

* Questions with readOnly=true should still Include their values

‘+ Questions in hidden views should be excluded (same visibilty logic)

9.2 Value Type Mapping

‘Component Type ‘Submission Value Format

INPUT_TEXT , TEXTAREA “string value"

RADIO_BUTTON , SELECT_ONE “selected_value"”

SELECT_MANY , CHECKBOX (mult) ["value1", “value2"]

TINPUT_NUMBER 42 or "42" (umber or string)

‘INPUT_NONEY {"amount": "100.00", "currency": “USD"}

‘INPUT_DATE )24-01-15

INPUT_DATE_NANY ["2024-01-15", "2024-02-20"]

LINPUT_PHONE_NUMBER {"countryCode": "#44", “phoneNumber": 7911123456")

INPUT_NANY [item", “iten2")

FILE_UPLOAD [{" FileName": “doc.pdt", "filevuid": "uuid-223"})

ADDRESS {addressLinel": "...", "city":

“countryCode": "GB", “postalCode'

TABLE [ercotr": "vat:

"cold": “val2"), «

TABLE_SEARCH {"'selectedid”

SIGNATURES ["name": "SIGNED" })

TIME_RANGE +

10. Error Handling

10.1 Client-Side Validation Errors

* Run validation rules before navigation (PUT) and submission (POST)

+ Display inline error messages next to the relevant question

‘+ Block navigation/submission if required fields are empty or validation fails

‘Scroll to the first error on the current view

10.2 Server-Side Validation Errors

‘Server-side validation runs on POST (submit) only. PUT and PATCH do not perform server-side validation

‘= On validation failure, POST returns HTTP 400 with a ValidationErrors JSON object containing error (string) and validationErrors (array of {field, error} objects)

‘© Mapthe field valuesin validationErrors back to question IDs and display the error messages inline next to the relevant question

10.3 Network Errors

* Display a generic error banner for network failures,

* Allow retry of the failed operation

+ Do not lose user-entered data on failure

10.4 Error Display

| Labet (toortip)

ee

| | User input)

=,

<= inline error

|! This field as required
11. Context Objects for Rule Evaluation

Rules can reference context data via $data, $user, $task:

Reference Source Description

$data.questionId Engine state ( values ) Current answer for that question

$data.questionId.nestedField Enginestate( values ) Nested field within the answer

$user.context Host integration "INTERNAL" (STAFF) or "EXTERNAL" (CUSTOMER)

$task. country Host integration Case country code

$task. legalEntity Host integration Legal entity name

$task.created0n Host integration Case creation date (format: dd MMM yyyy )

$task. legalEntitySector Host integration Legal entity sector code

$task. balanceSheet Host integration Balance sheet code

$task. LineOfBusiness Host integration Line of business

See rule_spec.md for full details on expression syntax and available context variables.

11.1 Context Initialization

On form load:

1. Host provides USER object (from authentication/session)

2. Host provides TASK object (from the case/task context)

3. Engine initializes $data from the form definition's existing values

4. Engine makes all three contexts available to the rule evaluator

11.2 Context Updates

e $data updates whenever values changes (reactive)

e $user and $task are static for the lifetime of the form session

