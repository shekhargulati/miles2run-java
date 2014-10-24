'use strict';

angular.module('milestogo')
    .controller('ViewActivityCtrl', function ($scope, $routeParams, ActivityService, $location, $modal, TimelineService, activeGoal, ConfigService) {
        var activityId = $routeParams.activityId;

        $scope.activity = {};

        ActivityService.get(activityId, activeGoal.id).success(function (data) {
            $scope.activity = data;
        }).error(function (data) {
            toastr.error("Unable to fetch activity with id: " + activityId);
            $location.path('/');

        });

        $scope.delete = function () {
            var modalIntance = $modal.open({
                templateUrl: "confirm.html",
                controller: ViewActivityDeleteActivityCtrl,
                resolve: {
                    activityToDelete: function () {
                        return $scope.activity;
                    }
                }
            })

        };

        TimelineService.goalTimeline(activeGoal.id, 1).success(function (data, status, headers, config) {
            $scope.activities = data.activities;
        }).error(function (data, status, headers, config) {
            toastr.error("Unable to fetch activities. Please try after sometime.");
        });

        $scope.appContext = ConfigService.appContext();

        $scope.messageToShare = function (activity) {
            return activity.fullname + ' ran ' + activity.distanceCovered + ' ' + activity.goalUnit + ' &via=miles2runorg';
        };

        $scope.redirectUri = function () {
            return ConfigService.absUrl();
        }

        $scope.facebookAppId = function () {
            if ($location.host() === "localhost") {
                return 433218286822536;
            } else if ($location.host() === "www.miles2run.org") {
                return 1466042716958015;
            }
            return 1441151639474875;
        }

        $scope.facebookShareUrl = function () {
            return "https://www.facebook.com/dialog/feed?redirect_uri=" + $scope.redirectUri() + "&link=" + $scope.activityUrl($scope.activity) + "&display=popup&description=" + $scope.messageToShare($scope.activity) + "&app_id=" + $scope.facebookAppId();
        }

        $scope.twitterShareUrl = function () {
            return "https://twitter.com/intent/tweet?url=" + $scope.activityUrl($scope.activity) + "&text=" + $scope.messageToShare($scope.activity);
        }

        $scope.googleShareUrl = function () {
            return "https://plus.google.com/share?url=" + $scope.activityUrl($scope.activity);
        }

        $scope.activityUrl = function (activity) {
            return ConfigService.absUrl() + 'profiles/' + activity.username + '/activities/' + activity.id;
        }


    });

var ViewActivityDeleteActivityCtrl = function ($scope, ActivityService, $modalInstance, activityToDelete, $rootScope, activeGoal, $location, $route) {

    $scope.ok = function () {
        ActivityService.deleteActivity(activityToDelete.id, activeGoal.id).success(function (data, status) {
            toastr.success("Deleted activity");
            $rootScope.$broadcast('update.progress', 'true');
            $modalInstance.close({});
            $location.path('/timeline');
            $route.reload();
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