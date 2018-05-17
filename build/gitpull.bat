set argC=0
for %%x in (%*) do Set /A argC+=1
if %argC%==1 (
	if exist %~1 (
		cd %~1
		echo.
		echo "success!"
		git pull
	) else (
		echo.
		echo "failed! - nonexisting directory"
		echo %~1
	)
) else (
	echo.
	echo "failed! - incorrect arguments count
	echo %argC%
)
