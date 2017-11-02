set argC=0
for %%x in (%*) do Set /A argC+=1
if %argC%==2 (
	if exist %~1 (
		cd %~1
		echo.
		echo "success!"
		git add .
		git commit -m "%~2"
		git push
	) else (
		echo.
		echo "failed! - nonexisting directory"
		echo %~1
	)
) else (
	echo.
	echo "failed! - incorrect arguments count
	echo %~1
)