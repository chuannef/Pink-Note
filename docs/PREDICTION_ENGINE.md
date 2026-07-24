# PinkNote Medical Prediction Engine

## Goal

PinkNote predictions are estimates for personal health tracking. The app must never present menstruation, ovulation, or fertility windows as certain facts.

The engine prioritizes:

- user-confirmed period days over automatic prediction
- late-period state instead of automatically starting a new cycle
- weighted historical learning from recent cycles
- clear uncertainty and medical disclaimer

## Architecture

The engine lives in the domain layer:

- `PredictCycleUseCase`: pure prediction logic
- `CyclePrediction`: prediction output for Home, Prediction, Calendar, reminders, and future AI
- `DailyLog.isPeriodDay`: user confirmation override
- `CycleRepository`: offline-first source of cycle settings and daily logs
- `HomeViewModel` and `CalendarViewModel`: combine Room/Firebase-backed data with the use case

This keeps UI code simple and lets tests verify prediction behavior without Firebase or Android UI.

## Algorithm

### First-time user

If the app only has the latest period start date:

1. Use default cycle length `28`.
2. Use default menstruation length `5`.
3. Estimate next period as `lastPeriodStart + weightedCycleLength`.
4. Estimate ovulation as about `14 days before next estimated period`.
5. Build fertile window around the estimated ovulation date.
6. Mark confidence as `VERY_LOW`.

### Confirmed cycle history

PinkNote groups consecutive `DailyLog` records where `isPeriodDay == true`.

Each group becomes one period record:

- group first date = actual period start
- group size = actual menstruation duration

Cycle length is calculated from one actual period start to the next.

### Weighted learning

Recent cycles matter more than old cycles:

- latest cycle: `40%`
- previous cycle: `30%`
- older cycle: `20%`
- oldest cycle: `10%`

Only the latest four cycles are used in the current deterministic model.

### Late period

If today is after the estimated period start and the user has not confirmed a new period:

1. Do not automatically create a new cycle.
2. Keep the estimated period start unchanged.
3. Increase `lateDays`.
4. Mark today and the late span as `LATE_PERIOD`.
5. Show possible reasons such as stress, travel, hormonal changes, illness, and lifestyle changes.

### Fertility probability

PinkNote does not show "safe" or "dangerous" days. It shows estimated probability bands:

- `VERY_LOW`
- `LOW`
- `MEDIUM`
- `HIGH`
- `VERY_HIGH`
- `PEAK`

The base window uses the common clinical estimate of five days before ovulation through one day after ovulation. Large cycle variability widens the displayed window.

Example deterministic estimates:

- 5 days before: `8%`
- 4 days before: `18%`
- 3 days before: `28%`
- 2 days before: `35%`
- 1 day before: `42%`
- estimated ovulation day: `38%`
- 1 day after: `10%`

These percentages are educational estimates, not guaranteed pregnancy probabilities.

### Confidence

Confidence is based on confirmed history count and cycle variability:

- `VERY_LOW`: no confirmed cycle interval
- `LOW`: little data or high variation
- `MEDIUM`: at least two recent cycles with moderate variation
- `HIGH`: at least three recent cycles with low variation

Even `HIGH` is still an estimate.

## Firestore Schema

Current scalable collections:

```text
users/{uid}
cycle/{uid}
daily_logs/{uid}/days/{yyyy-MM-dd}
notifications/{uid}/items/{notificationId}
settings/{uid}
```

Recommended future additions:

```text
period_records/{uid}/records/{periodId}
cycle_predictions/{uid}/items/{predictionId}
fertility_estimates/{uid}/days/{yyyy-MM-dd}
analytics/{uid}/monthly/{yyyy-MM}
```

`daily_logs` now supports:

```text
isPeriodDay: true | false | null
```

Meaning:

- `true`: user confirms menstruation
- `false`: user confirms no menstruation
- `null`: no confirmation, prediction may be used

## Notifications

Prediction outputs can drive:

- reminder before estimated period
- reminder before fertile window
- reminder to confirm period start
- late-period reminder

Late reminders should ask the user to update the actual period start instead of assuming the cycle has started.

## Future AI Improvements

Future models can learn from:

- period start/end confirmations
- cycle length variance
- PMS symptoms
- pain level
- mood
- body temperature
- medication
- travel/lifestyle tags

AI should assist prediction and explanation only. It should not provide diagnosis or replace medical advice.

## Medical Disclaimer

PinkNote is intended for educational and personal health tracking purposes only. It does not provide medical diagnosis, treatment, or professional medical advice. Predictions are estimates based on user-entered data and statistical models. PinkNote should not be used as a method of contraception or pregnancy planning. Users should consult a qualified healthcare professional for medical concerns.
