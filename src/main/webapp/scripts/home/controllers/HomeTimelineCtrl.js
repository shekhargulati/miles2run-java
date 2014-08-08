'use strict';

angular.module('miles2run-home')
    .controller('HomeTimelineCtrl', function ($scope, $http, $modal, $location, activeProfile, ConfigService, ActivityService, $window) {
        $scope.currentUser = activeProfile;
        $scope.forms = {};
        $scope.forms.selectedGoal = {};

        $scope.activity = {
            goalUnit: 'MI',
            share: {}
        };

        $scope.duration = {

        };

        $scope.goalExists = false;

        $http.get(ConfigService.getBaseUrl() + "goals").success(function (data) {
            if (isEmpty(data)) {
                $scope.goalExists = false;
            } else {
                $scope.goalExists = true;
                $scope.goals = data;
                $scope.forms.selectedGoal = $scope.goals[0];
            }
        }).error(function (data, status) {
            $scope.goalExists = false;
            toastr.error("Unable to fetch goals. Please try after sometime.");
            console.log("Error " + data);
            console.log("Status " + status)
        });

        var isEmpty = function (obj) {
            return  Boolean(obj && typeof obj == 'object') && !Object.keys(obj).length;
        }

        $scope.buttonText = "Log your Run";

        $scope.validateDuration = function (duration) {
            var durationVal = toAppSeconds(duration)
            if (durationVal > 0) {
                $scope.forms.activityForm.durationHours.$invalid = false;
            } else {
                $scope.forms.activityForm.durationHours.$invalid = true;
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


        $scope.postActivity = function () {
            console.log($scope.activity);
            $scope.submitted = true;
            $scope.validateDuration($scope.duration);
            if ($scope.forms.activityForm.$valid && !$scope.forms.activityForm.durationHours.$invalid) {
                $scope.successfulSubmission = true;
                $scope.buttonText = "Logging your run..";
                $scope.activity.duration = toAppSeconds($scope.duration);
                ActivityService.postActivity($scope.activity, $scope.forms.selectedGoal.id).success(function (data, status, headers, config) {
                    toastr.success("Posted new activity");
                    $window.location.href = ConfigService.appContext() + 'goals/' + $scope.forms.selectedGoal.id;
                }).error(function (data, status, headers, config) {
                    console.log("Error handler for PostActivity. Status code " + status);
                    toastr.error("Unable to post activity. Please try later.");
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
            $scope.minDate = null;
            var current = new Date();
            $scope.maxDate = current;

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

        $scope.formats = ['dd-MMMM-yyyy', 'yyyy/MM/dd', 'shortDate'];
        $scope.format = $scope.formats[0];


        if (!angular.isDefined($scope.currentPage)) {
            $scope.currentPage = 1;
        }
        $scope.homeTimelinePromise = $http.get(ConfigService.getBaseUrl() + 'activities/home_timeline').success(function (data, status, headers, config) {
            $scope.activities = data.timeline;
            $scope.totalItems = data.totalItems;
        }).error(function (data, status, headers, config) {
            toastr.error("Unable to fetch home timeline. Please try after sometime.");
        });

        $scope.pageChanged = function () {
            console.log('Page changed to: ' + $scope.currentPage);
            $http.get(ConfigService.getBaseUrl() + 'activities/home_timeline', {params: {page: $scope.currentPage}}).success(function (data, status, headers, config) {
                $scope.activities = data.timeline;
                $scope.totalItems = data.totalItems;
            }).error(function (data, status, headers, config) {
                toastr.error("Unable to fetch home timeline. Please try after sometime.");
            });
        };

        $scope.messageToShare = function (activity) {
            return activity.fullname + ' ran ' + activity.distanceCovered + ' ' + activity.goalUnit + ' &via=miles2runorg';
        };

        $scope.redirectUri = function () {
            return ConfigService.absUrl();
        }

        $scope.facebookAppId = function () {
            if ($location.host() === "localhost") {
                return 433218286822536;
            }
            return 1441151639474875;
        }

        $scope.facebookShareUrl = function (activity) {
            return "https://www.facebook.com/dialog/feed?redirect_uri=" + $scope.redirectUri() + "&link=" + $scope.activityUrl(activity) + "&display=popup&description=" + $scope.messageToShare(activity) + "&app_id=" + $scope.facebookAppId();
        }

        $scope.twitterShareUrl = function (activity) {
            return "https://twitter.com/intent/tweet?url=" + $scope.activityUrl(activity) + "&text=" + $scope.messageToShare(activity);
        }

        $scope.googleShareUrl = function (activity) {
            return "https://plus.google.com/share?url=" + $scope.activityUrl(activity);
        }

        $scope.activityUrl = function (activity) {
            return ConfigService.absUrl() + 'profiles/' + activity.username + '/activities/' + activity.id;
        }

        $scope.delete = function (idx) {
            var modalIntance = $modal.open({
                templateUrl: "confirm.html",
                controller: DeleteActivityCtrl,
                resolve: {
                    activityToDelete: function () {
                        var activityToDelete = $scope.activities[idx];
                        return activityToDelete;
                    },
                    idx: function () {
                        return idx;
                    },
                    activities: function () {
                        return $scope.activities;
                    }
                }
            })

        };

    });

var DeleteActivityCtrl = function ($scope, $modalInstance, activityToDelete, idx, activities, ConfigService, $http) {

    $scope.ok = function () {
        $http.delete(ConfigService.getBaseUrl() + "goals/" + activityToDelete.goalId + "/activities/" + activityToDelete.id).success(function (data, status) {
            toastr.success("Deleted activity");
            activities.splice(idx, 1);
            $modalInstance.close({});
        }).error(function (data, status, headers, config) {
            console.log("Status code %s", status);
            if (status == 401) {
                toastr.error("You are not authorized to perform this operation.")
            } else {
                toastr.error("Unable to delete activity. Please try later.");
            }
            $modalInstance.close({});
        });
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
};