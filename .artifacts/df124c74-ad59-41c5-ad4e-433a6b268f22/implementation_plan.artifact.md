# Implementation Plan - Enhanced Alarm Features

This plan outlines the steps to add seconds to the alarm setting, implement custom music selection, and fix the tone selection bug.

## User Review Required

> [!IMPORTANT]
> The standard Android `TimePicker` does not support seconds. I will replace it with three `NumberPicker` components (Hour, Minute, Second) in the "Add Alarm" dialog to allow precise time setting.

> [!NOTE]
> For music selection, I will use `Intent.ACTION_OPEN_DOCUMENT`. This requires the app to persist URI permissions to access the file later when the alarm rings.

## Proposed Changes

### Core Data Model
#### [MODIFY] [AlarmModel.java](file:///C:/Users/as705/StudioProjects/ALARM-CLOCK-APP/app/src/main/java/com/example/aeroalarm/AlarmModel.java)
- Add `int second` field.
- Add getter and setter for `second`.

---

### UI Components
#### [MODIFY] [dialog_add_alarm.xml](file:///C:/Users/as705/StudioProjects/ALARM-CLOCK-APP/app/src/main/res/layout/dialog_add_alarm.xml)
- Replace `TimePicker` with a horizontal `LinearLayout` containing three `NumberPicker`s (Hour, Minute, Second) and an AM/PM toggle or picker.
- Add a button "Select Custom Music" next to or instead of the Tone spinner.

#### [MODIFY] [item_alarm.xml](file:///C:/Users/as705/StudioProjects/ALARM-CLOCK-APP/app/src/main/res/layout/item_alarm.xml) (Verification needed if layout supports extra space)
- Ensure the time display can accommodate `HH:mm:ss`.

---

### Logic & Helpers
#### [MODIFY] [AlarmManagerHelper.java](file:///C:/Users/as705/StudioProjects/ALARM-CLOCK-APP/app/src/main/java/com/example/aeroalarm/AlarmManagerHelper.java)
- Update `scheduleAlarm` to use `calendar.set(Calendar.SECOND, alarm.getSecond())`.

#### [MODIFY] [MainActivity.java](file:///C:/Users/as705/StudioProjects/ALARM-CLOCK-APP/app/src/main/java/com/example/aeroalarm/MainActivity.java)
- Update `showAlarmDialog` to initialize and read values from the new `NumberPicker`s.
- Implement file picker logic for music selection.
- Handle URI permission persistence for selected music files.
- Update the `tones` spinner to include a "Custom..." option.

#### [MODIFY] [AlarmAdapter.java](file:///C:/Users/as705/StudioProjects/ALARM-CLOCK-APP/app/src/main/java/com/example/aeroalarm/AlarmAdapter.java)
- Update `onBindViewHolder` to display time as `HH:mm:ss`.

#### [MODIFY] [RingingActivity.java](file:///C:/Users/as705/StudioProjects/ALARM-CLOCK-APP/app/src/main/java/com/example/aeroalarm/RingingActivity.java)
- Fix the bug: instead of playing the default alarm tone, use `currentAlarm.getTone()`.
- Handle both default tone names (by mapping them to system sounds) and URI-based custom music.

---

### Permissions
#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/as705/StudioProjects/ALARM-CLOCK-APP/app/src/main/AndroidManifest.xml)
- Ensure necessary permissions for reading external storage (if needed for older Android versions) or just use the Storage Access Framework (SAF) which is preferred.

## Verification Plan

### Automated Tests
- None planned as this is mostly UI and system interaction.

### Manual Verification
1. **Set Alarm with Seconds**: Create an alarm for 5 seconds from now and verify it rings exactly at that second.
2. **Select Custom Music**: Pick an MP3 file from the device storage, set it as the alarm tone, and verify it plays when the alarm rings.
3. **Tone Selection**: Select different default tones and verify that each one plays its unique sound.
4. **Persistency**: Close the app and verify the custom music still plays when the alarm triggers.
