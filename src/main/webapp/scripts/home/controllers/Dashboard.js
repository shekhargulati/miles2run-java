'use strict';

angular.module('milestogo')
    .controller('DashboardCtrl', function ($scope, ProgressService, ConfigService, activeProfile, $http, $timeout) {
        $scope.currentUser = activeProfile;
        $scope.error = null;
        $scope.data = {};

        ProgressService.progress($scope.currentUser.username).success(function (data, status, headers, config) {
            $scope.error = null;
            $scope.status = status;
            $scope.data = data;
            $scope.style = "width:" + data.percentage + "%";
        }).
            error(function (data, status, headers, config) {
                toastr.error("Unable to fetch your progress. Please try later.");
                $scope.error = {message: "Unable to fetch your progress. Please try later."};
                console.log(data);
                console.log(status);
            });

        var renderDistanceChart = function (interval) {
            $("#activities-line-graph").empty();
            console.log('User selected interval ' + interval);
            $http.get("api/v1/dashboard/charts/distance?interval=" + interval).success(function (data, status, headers, config) {
                Morris.Line({
                    element: 'activities-line-graph',
                    data: data,
                    xLabels: interval,
                    xLabelFormat: function (date) {
                        if (interval === "month") {
                            return moment(dateFormat(date.getTime()), "YYYYMMDD").format("MMM YYYY");
                        } else if (interval === "year") {
                            return date.getFullYear().toString();
                        }
                        return moment(dateFormat(date.getTime()), "YYYYMMDD").format('MMM Do');
                    },
                    yLabelFormat: function (distance) {
                        return distance.toString() + ' ' + activeProfile.goalUnit.$name;
                    },
                    xkey: interval,
                    ykeys: ['distance'],
                    dateFormat: function (x) {
                        if (interval === "month") {
                            return moment(dateFormat(x), "YYYYMMDD").format("MMM YYYY");
                        } else if (interval === "year") {
                            return new Date(x).getFullYear().toString();
                        }
                        return moment(dateFormat(x), "YYYYMMDD").format('dddd, MMM Do YYYY');
                    },
                    labels: ['Distance Ran']

                });
            }).error(function (data, status, headers, config) {
                console.log(data);
            });
        }

        renderDistanceChart("day");
        $scope.renderDistanceChart = renderDistanceChart;

        var renderPaceChart = function (interval) {
            $("#pace-line-graph").empty();
            console.log('User selected interval ' + interval);
            $http.get("api/v1/dashboard/charts/pace?interval=" + interval).success(function (data, status, headers, config) {
                Morris.Line({
                    element: 'pace-line-graph',
                    data: data,
                    xLabels: interval,
                    xLabelFormat: function (date) {
                        if (interval === "month") {
                            return moment(dateFormat(date.getTime()), "YYYYMMDD").format("MMM YYYY");
                        } else if (interval === "year") {
                            return date.getFullYear().toString();
                        }
                        return moment(dateFormat(date.getTime()), "YYYYMMDD").format('MMM Do');
                    },
                    yLabelFormat: function (pace) {
                        return pace.toString() + ' mins/' + activeProfile.goalUnit.$name;
                    },
                    xkey: interval,
                    ykeys: ['pace'],
                    dateFormat: function (x) {
                        if (interval === "month") {
                            return moment(dateFormat(x), "YYYYMMDD").format("MMM YYYY");
                        } else if (interval === "year") {
                            return new Date(x).getFullYear().toString();
                        }
                        return moment(dateFormat(x), "YYYYMMDD").format('dddd, MMM Do YYYY');
                    },
                    labels: ['Pace']

                });
            }).error(function (data, status, headers, config) {
                console.log(data);
            });
        }

        renderPaceChart("day");
        $scope.renderPaceChart = renderPaceChart;

        var dateFormat = function (x) {
            var date = new Date(x);
            var yyyy = date.getFullYear().toString();
            var mm = (date.getMonth() + 1).toString();
            var dd = date.getDate().toString();
            return yyyy + "-" + (mm[1] ? mm : "0" + mm[0]) + "-" + (dd[1] ? dd : "0" + dd[0]);
        }

    });