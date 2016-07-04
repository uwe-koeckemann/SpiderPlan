%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Object Properties
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
hasNodeClass(node1,lowPowerProcessor).
hasNodeClass(node2,lowPowerProcessor).

hasNodeClass(robot,generalPurposeProcessor).

hasNodeClass(Node,lowPowerProcessor) :- hasNodeClass(Node,generalPurposeProcessor).

isRobot(robot).
isMobile(robot).
canRotate(robot).

adjacencyTable(livingRoom,kitchen).
adjacencyTable(livingRoom,bathroom).
adjacencyTable(livingRoom,bedroom).

adjacent(A,B) :- adjacencyTable(A,B).
adjacent(A,B) :- adjacencyTable(B,A).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Basic concepts directly from sensors
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
capabilityBase(lowPowerProcessor, doorSensor(Door), none, doorState(Door) ) :- door(Door).
capabilityBase(lowPowerProcessor, windowSensor(Window), none, windowState(Window) ) :- window(Window).
capabilityBase(lowPowerProcessor, pressureSensor(Chair), none, chairState(Chair)) :- chair(Chair).
capabilityBase(lowPowerProcessor, pressureSensor(Bed), none, bedState(Bed)) :- bed(Bed).

capabilityBase(lowPowerProcessor, pir(Location), none, humanPressence(Location)) :- location(Location).

capabilityBase(lowPowerProcessor, environmentalSensor(Location), none, temperature(Location)) :- location(Location).
capabilityBase(lowPowerProcessor, environmentalSensor(Location), none, humidity(Location)) :- location(Location).
capabilityBase(lowPowerProcessor, luminositySensor(Location), none, luminosity(Location)) :- location(Location).

capabilityBase(lowPowerProcessor, waterCurrentSensor(WaterTab), none, using(WaterTap)) :- waterTap(WaterTap).
capabilityBase(lowPowerProcessor, waterCurrentSensor(Shower), none, using(Shower)) :- shower(Shower).

capabilityBase(lowPowerProcessor, electricCurrentSensor(TV), none, using(TV)) :- tv(TV).
capabilityBase(lowPowerProcessor, electricCurrentSensor(Stove), none, using(Stove)) :- stove(TV).

capabilityBase(lowPowerProcessor, activityTracker(Human), none, activityLevel(Human)) :- human(Human).

capabilityBase(generalPurposeProcessor, camera(Location), none, humanPressence(Location)).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Using installed cameras
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
capabilityBase(generalPurposeProcessor, camera(bedroom), facingEntityAtLocation(bedJohn), bedState(bedJohn)).

capabilityBase(generalPurposeProcessor, camera(kitchen), facingEntityAtLocation(kitchenChair), chairState(kitchenChair)).
capabilityBase(generalPurposeProcessor, camera(kitchen), facingEntityAtLocation(fridgeDoor), doorState(fridgeDoor)).
capabilityBase(generalPurposeProcessor, camera(kitchen), facingEntityAtLocation(stoveJohn), using(stoveJohn)).

capabilityBase(generalPurposeProcessor, camera(livingRoom), facingEntityAtLocation(tvJohn), using(tvJohn)).
capabilityBase(generalPurposeProcessor, camera(livingRoom), facingEntityAtLocation(tvJohn), using(tvJohn)).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Using the robot
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
capabilityBase(generalPurposeProcessor, robot, facingEntityAtLocation(Human), activity(Human)).

capabilityBase(generalPurposeProcessor, robot, atLocation(Location), airQuality(Location)).

capabilityBase(generalPurposeProcessor, robot, facingEntityAtLocation(Human), smoking(Human)).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Concepts Requiring 2 Others
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
capabilityTwo(lowPowerProcessor, temperature(Location), humidity(Location), airQuality(Location)).

capabilityTwo(lowPowerProcessor, doorState(fridgeDoor), chairState(kitchenChair), eating(john)).

capabilityTwo(lowPowerProcessor, bedState(bedJohn), luminosity(bedroom), sleeping(john)).

capabilityTwo(lowPowerProcessor, humanPressence(kitchen), using(stoveJohn), cooking(john)).

capabilityTwo(lowPowerProcessor, humanPressence(bathroom), using(showerJohn), showering(john)).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Concepts Requiring 3 Others
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Concepts Requiring 4 Others
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Energy Consumption
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
deviceEnergyConsumption(Robot,20) :- isRobot(Robot).
deviceEnergyConsumption(Node,10) 	:- nodeID(Node), hasNodeClass(generalPurposeProcessor).
deviceEnergyConsumption(Node,3) 	:- nodeID(Node), hasNodeClass(lowPowerProcessor).

deviceEnergyConsumption(environmentalSensor(Location), 5) :- location(Location).

deviceEnergyConsumption(Sensor,2) :- sensorID(Sensor).




