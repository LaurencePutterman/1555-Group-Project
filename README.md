# 1555-Group-Project
Milestone 2:
Please use DriverProgram to test
Milestone 3:
Fixed from previous milestones:
-DB Schema now makes use of a function and a procedure
-cancelReservation now downsizes the plane if it can (before this functionality was delegated to planeUpgrade, but didn't quite work right)
-File input functions take CSV files (this has been the case all along, but we received this as a comment on our second milestone)
-Customer task 8 now checks to make sure there is space on the flight and returns an error message to the user if the flight is full (note that before this occurs the triggers will move the flight to a larger plane if the current plane is full and a bigger one is available)