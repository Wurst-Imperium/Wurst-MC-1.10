@echo off
rem Batch script for applying the Minecraft patch.
rem Requires a local repo with the Minecraft source code.

rem Branches:
rem master    vanilla MC
rem Wurst     modified Wurst MC
rem tmp       temporary branch (will be created & deleted automatically)

rem Change the paths to match your workspace layout.
set mc-dir="C:\Users\alexa\OneDrive\Wurst Client\Wurst-Client-for-MC-1.10.X\Minecraft 1.10.X"
set wurst-dir="C:\Users\alexa\OneDrive\Wurst Client\Wurst-Client-for-MC-1.10.X\Wurst Client for MC 1.10.X"

cd %mc-dir%
git checkout master
git apply --ignore-space-change --ignore-whitespace %wurst-dir%\patch\minecraft.patch
xcopy src tmp /E /Y /I
git reset --hard
git checkout Wurst
xcopy tmp src /E /Y /I
rem pause