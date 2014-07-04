'use strict';

function CreateGoalCtrl($scope, $location, activeProfile, $http, ConfigService, $window) {

    $scope.currentUser = activeProfile;

    $scope.goal = {
        goalUnit: 'MI'
    };

    $scope.buttonText = "Create";

    $scope.createGoal = function () {
        $scope.submitted = true;
        if ($scope.goalForm.$valid) {
            $scope.successfulSubmission = true;
            $scope.buttonText = "Creating Goal..";
            $scope.createGoalPromise =  $http.post(ConfigService.getBaseUrl() + "goals", $scope.goal).success(function (data) {
                toastr.success("Created new goal");
                $window.location.href = ConfigService.appContext() + 'goals/' + data.id;
            }).error(function (data, status) {
                toastr.error("Unable to create goal. Please try after sometime.");
                console.log("Error " + data);
                console.log("Status " + status)
                $location.path("/");
            });
        }

    };

    $scope.today = function () {
        $scope.dt = new Date();
    };
    $scope.today();

    $scope.showWeeks = true;
    $scope.toggleWeeks = function () {
        $scope.showWeeks = !$scope.showWeeks;
    };

    $scope.clear = function () {
        $scope.dt = null;
    };

    // Disable weekend selection
    $scope.disabled = function (date, mode) {
        return ( mode === 'day' && ( date.getDay() === 0 || date.getDay() === 6 ) );
    };

    $scope.toggleMin = function () {
        $scope.minDate = ( $scope.minDate ) ? null : new Date();
    };
    $scope.toggleMin();

    $scope.open = function ($event) {
        $event.preventDefault();
        $event.stopPropagation();

        $scope.opened = true;
    };

    $scope.dateOptions = {
        'year-format': "'yy'",
        'starting-day': 1
    };
}

angular.module('miles2run-home')
    .controller('CreateGoalCtrl', CreateGoalCtrl);
