// Sey Morphe patches - Twitch TV default home tab.
//
// Twitch TV opens the remote Next.js UI on the same tab every launch. This
// helper re-routes the app to a chosen tab (Home, Following, Live or
// Categories) whenever the page loads, so the app always starts on the tab
// the user picked in SEYTTINGS.
//
// Distributed as part of the Sey Morphe patches under the GNU GPL v3.
(function () {
    'use strict';

    var MARK = 'data-sey-hometab';
    var BOOT = 'window.__seyHomeTab';
    var INTERVAL_MS = 1500;

    // Twitch TV routes for the top-level tabs. The exact path depends on the
    // remote build, so each entry is tried in order: a nav link is clicked
    // first and the location is rewritten only as a fallback.
    var ROUTES = {
        'Home': ['/', '/home'],
        'Following': ['/following', '/followed-channels'],
        'Live': ['/live', '/categories'],
        'Categories': ['/categories', '/live']
    };

    var enabled = false;
    var tab = 'Home';
    var pending = null;

    function notify(message) {
        try {
            if (window.seyttingsHomeTab && window.seyttingsHomeTab.report) {
                window.seyttingsHomeTab.report(String(message));
            }
        } catch (e) { /* bridge unavailable */ }
        try {
            console.log('[SEYTTINGS hometab] ' + message);
        } catch (e) { /* console unavailable */ }
    }

    function routeFor(tabName) {
        var routes = ROUTES[tabName];
        return routes && routes.length ? routes[0] : '/';
    }

    function currentPath() {
        try {
            return location.pathname || '/';
        } catch (e) {
            return '/';
        }
    }

    // Clicks a nav link whose href matches one of the target routes. Next.js
    // handles the click client-side, so the URL bar and the view change
    // without a full reload.
    function clickNavLink(route) {
        var i;
        var links = document.querySelectorAll('a');
        for (i = 0; i < links.length; i++) {
            var href = links[i].getAttribute('href') || '';
            if (href === route || href.indexOf(route) === 0) {
                try {
                    links[i].click();
                } catch (e) { /* link not clickable yet */ }
                return true;
            }
        }
        return false;
    }

    function navigateTo(tabName) {
        var routes = ROUTES[tabName];
        if (!routes || routes.length === 0) {
            return false;
        }
        var path = currentPath();
        var i;
        for (i = 0; i < routes.length; i++) {
            if (path === routes[i] || path.indexOf(routes[i] + '/') === 0) {
                // Already on the target tab.
                return true;
            }
        }
        // Try clicking a nav link first (client-side navigation).
        for (i = 0; i < routes.length; i++) {
            if (clickNavLink(routes[i])) {
                notify('Default home tab: ' + tabName + ' -> ' + routes[i]);
                return true;
            }
        }
        // Fallback: rewrite the location so the router picks it up.
        try {
            history.pushState(null, '', routes[0]);
            location.reload();
        } catch (e) {
            try {
                location.href = routes[0];
            } catch (e2) { /* navigation unavailable */ }
        }
        notify('Default home tab: ' + tabName + ' -> ' + routes[0]);
        return true;
    }

    function apply() {
        if (!enabled) {
            return;
        }
        try {
            navigateTo(tab);
        } catch (e) { /* never throw into the page */ }
    }

    function setEnabled(on) {
        on = !!on;
        if (on === enabled) {
            return;
        }
        enabled = on;
        if (on) {
            apply();
            notify('Default home tab enabled (' + tab + ')');
        } else {
            notify('Default home tab disabled');
        }
    }

    function setTab(tabName) {
        var known = ROUTES[tabName];
        if (!known) {
            return;
        }
        tab = tabName;
        if (enabled) {
            apply();
        }
        notify('Default home tab set to ' + tabName);
    }

    if (typeof window !== 'undefined' && window.top === window && !window[BOOT]) {
        window[BOOT] = true;

        try {
            if (window.seyttingsHomeTab && window.seyttingsHomeTab.isEnabled) {
                enabled = !!window.seyttingsHomeTab.isEnabled();
            }
        } catch (e) { /* native bridge unavailable */ }
        try {
            if (window.seyttingsHomeTab && window.seyttingsHomeTab.getHomeTab) {
                var saved = window.seyttingsHomeTab.getHomeTab();
                if (saved && ROUTES[saved]) {
                    tab = saved;
                }
            }
        } catch (e) { /* native bridge unavailable */ }

        // Expose a small API for the native side and for diagnostics.
        window.__seyHomeTabApply = apply;
        window.__seyHomeTabSetEnabled = setEnabled;
        window.__seyHomeTabSetTab = setTab;
        window.__seyHomeTabState = function () {
            return { enabled: enabled, tab: tab };
        };

        if (enabled) {
            apply();
        }

        // Re-apply after client-side navigation and React re-renders.
        try {
            window.addEventListener('popstate', function () {
                try { apply(); } catch (e) { /* ignore */ }
            });
        } catch (e) { /* ignore */ }
        pending = window.setInterval(function () {
            try { apply(); } catch (e) { /* ignore */ }
        }, INTERVAL_MS);

        notify('Default home tab installed, enabled=' + enabled + ' tab=' + tab);
    }
})();
