'use strict';

function CreateGoalCtrl($scope, $location) {
    $scope.renderNextView = function () {
        console.log($scope.goalType);
        if ($scope.goalType === 'duration_goal') {
            $location.path("/goals/create_duration_goal");
        } else if ($scope.goalType === 'distance_goal') {
            $location.path("/goals/create_distance_goal");
        } else {
            $location.path("/goals/create_distance_goal");
        }

    }

}

angular.module('miles2run-home')
    .controller('CreateGoalCtrl', CreateGoalCtrl);
