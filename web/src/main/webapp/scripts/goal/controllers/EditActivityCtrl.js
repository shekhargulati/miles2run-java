'use strict';

angular.module('milestogo')
    .controller('EditActivityCtrl', function ($scope, $routeParams, ActivityService, $location, $rootScope, activeGoal) {
        var activityId = $routeParams.activityId;

        $scope.buttonText = "Update";

        ActivityService.get(activityId, activeGoal.id).success(function (data) {
            $scope.activity = data;
            $scope.activity.activityDate = new Date(data.activityDate);
            $scope.duration = toHrMinSec(data.duration);
        }).error(function (data) {
            toastr.error("Unable to fetch activity with id: " + activityId);
            $location.path('/');

        });

        $scope.validateDuration = function (duration) {
            var durationVal = toAppSeconds(duration)
            if (durationVal >= 0) {
                $scope.activityForm.durationHours.$invalid = false;
            } else {
                $scope.activityForm.durationHours.$invalid = true;
            }
        }

        $scope.validateDurationForDistanceGoal = function (duration) {
            var durationVal = toAppSeconds(duration)
            if (durationVal > 0) {
                $scope.activityForm.durationHours.$invalid = false;
            } else {
                $scope.activityForm.durationHours.$invalid = true;
            }
        }

        var toAppSeconds = function (duration) {
            if (duration) {
                var hours = duration.hours && duration.hours !== '00' ? duration.hours : 0;
                var minutes = duration.minutes && duration.minutes !== '00' ? duration.minutes : 0;
                var seconds = duration.seconds && duration.seconds !== '00' ? duration.seconds : 0;
                return Number(hours) * 60 * 60 + Number(minutes) * 60 + Number(seconds);
            }
            return 0;
        }

        var removeTime = function (d) {
            return new Date(d.getFullYear(), d.getMonth(), d.getDate())
        }

        $scope.updateActivity = function () {
            $scope.submitted = true;
            if (activeGoal.goalType.$name === 'DISTANCE_GOAL') {
                $scope.validateDurationForDistanceGoal($scope.duration);
            }
            if ($scope.activityForm.$valid && !$scope.activityForm.durationHours.$invalid) {
                $scope.successfulSubmission = true;
                $scope.buttonText = "Updating..";
                var duration = toAppSeconds($scope.duration);
                var activity = {
                    status: $scope.activity.status,
                    goalUnit: $scope.activity.goalUnit,
                    distanceCovered: $scope.activity.distanceCovered,
                    activityDate: $scope.activity.activityDate,
                    duration: duration
                };

                ActivityService.updateActivity(activityId, activity, activeGoal.id).success(function (data, status, headers, config) {
                    $rootScope.$broadcast('update.progress', 'true');
                    toastr.success("Updated activity");
                    $location.path('/activity/' + data.id);
                }).error(function (data, status, headers, config) {
                    console.log("Error handler for update activity. Status code " + status);
                    if (status == 401) {
                        toastr.error("You are not authorized to perform this operation.");
                    } else {
                        toastr.error("Unable to update activity. Please try later.");
                    }
                    $location.path('/');
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
            $scope.minDate = activeGoal.startDate ? new Date(activeGoal.startDate.time) : null;
            var current = new Date();
            $scope.maxDate = activeGoal.endDate ? (new Date(activeGoal.endDate.time) > current ? current : new Date(activeGoal.endDate.time)) : current;
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

    });


function toHrMinSec(duration) {
    var hours = Math.floor(duration / (60 * 60));
    var minutes = Math.floor(duration / 60) - (hours * 60);
    var seconds = duration - (minutes * 60) - (hours * 60 * 60);
    return  {
        hours: hours && hours !== 0 ? hours : 0,
        minutes: minutes && minutes != 0 ? minutes : 0,
        seconds: seconds && seconds != 0 ? seconds : 0
    }
}