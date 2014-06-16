'use strict';

angular.module('milestogo')
    .directive('dataTable', function () {
        function link(scope, el) {
            var config = scope.config;
            var element = el[0];
            $()
        }

        return {
            template: '<div id="cal-heatmap" align="center" config="config"></div>',
            restrict: 'E',
            link: link,
            scope: { config: '=' }
        };
    })
;
