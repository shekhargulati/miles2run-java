'use strict';

angular.module('milestogo')
    .controller('EditActivityCtrl', function ($scope, $routeParams, ActivityService, $location, $rootScope, activeGoal) {
        var activityId = $routeParams.activityId;

        $scope.buttonText = "Update your Run";

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

        var toAppSeconds = function(duration){
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
                $scope.buttonText = "Updating your run..";
                var activityDate = removeTime($scope.activity.activityDate).toDateString();
                var index = activityDate.indexOf(" ");
                activityDate = activityDate.substr(index + 1, activityDate.length);
                activityDate = moment(activityDate, "MMM DD yyyy").format("YYYY-MM-DD");

                var activity = {
                    id: $scope.activity.id,
                    status: $scope.activity.status,
                    goalUnit: $scope.activity.goalUnit,
                    distanceCovered: $scope.activity.distanceCovered,
                    share: $scope.activity.share,
                    activityDate: activityDate
                };
                activity.duration = toAppSeconds($scope.duration);
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