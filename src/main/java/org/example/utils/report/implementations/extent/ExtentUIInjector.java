package org.example.utils.report.implementations.extent;

/**
 * Utility to provide custom CSS and JS for the ExtentReports SparkReporter.
 */
public class ExtentUIInjector {

    public static String getCustomCss() {
        return """
            .test-report-wrapper { width: 100%; }
            .tab-container { display: flex; gap: 20px; border-bottom: 2px solid #e2e8f0; margin-bottom: 15px; }
            .tab-btn { padding: 8px 6px; cursor: pointer; font-size: 12.5px; font-weight: 700; color: #64748b; border-bottom: 3px solid transparent; transition: all 0.2s ease; }
            .tab-btn:hover { color: #1e293b; }
            .tab-btn.active { color: #e74c3c; border-bottom: 3px solid #e74c3c; }
            .tab-panel { display: none; }
            .tab-panel.active { display: block; }
            .report-table { width: 100%; border-collapse: collapse; background: #fff; border: 1px solid #e2e8f0; border-radius: 6px; overflow: hidden; }
            
            /* TAB DETAILS 3-Column Layout */
            .details-table { table-layout: fixed; width: 100%; }
            .details-table th:nth-child(1), .details-table td:nth-child(1) { width: 14% !important; vertical-align: top; padding: 8px 10px; }
            .details-table th:nth-child(2), .details-table td:nth-child(2) { width: 12% !important; vertical-align: top; padding: 8px 10px; font-family: monospace; font-size: 11.5px; color: #64748b; }
            .details-table th:nth-child(3), .details-table td:nth-child(3) { width: 74% !important; vertical-align: top; padding: 8px 10px; word-break: normal !important; overflow-wrap: break-word !important; white-space: normal !important; }
            
            /* TAB NETWORK LOGS 4-Column Layout */
            .network-table { table-layout: fixed !important; width: 100% !important; }
            .network-table th:nth-child(1), .network-table td:nth-child(1) { width: 10% !important; vertical-align: top; padding: 8px 10px; }
            .network-table th:nth-child(2), .network-table td:nth-child(2) { width: 12% !important; vertical-align: top; padding: 8px 10px; }
            .network-table th:nth-child(3), .network-table td:nth-child(3) { width: 12% !important; vertical-align: top; padding: 8px 10px; font-family: monospace; font-size: 11.5px; color: #64748b; }
            .network-table th:nth-child(4), .network-table td:nth-child(4) { width: 66% !important; vertical-align: top; padding: 8px 10px; word-break: break-word !important; overflow-wrap: break-word !important; }
            .network-detail-label { font-weight: 700; color: #475569; display: inline-block; min-width: 60px; }
            
            .report-table th { text-align: left; padding: 9px 10px; background: #f8fafc; border-bottom: 2px solid #e2e8f0; font-size: 11px; font-weight: 700; color: #475569; text-transform: uppercase; }
            .report-table td { padding: 8px 10px; border-bottom: 1px solid #f1f5f9; font-size: 12.5px; line-height: 1.5; }
            .report-table tr:hover td { background-color: #f8fafc; }
            
            /* Badges */
            .badge-info { background: #0ea5e9; color: white; padding: 2px 6px; border-radius: 4px; font-size: 10.5px; font-weight: 700; }
            .badge-pass { background: #10b981; color: white; padding: 2px 6px; border-radius: 4px; font-size: 10.5px; font-weight: 700; }
            .badge-fail { background: #ef4444; color: white; padding: 2px 6px; border-radius: 4px; font-size: 10.5px; font-weight: 700; flex: 0 0 auto; }
            .badge-skip { background: #f59e0b; color: white; padding: 2px 6px; border-radius: 4px; font-size: 10.5px; font-weight: 700; }
            .badge-broken { background: #8b5cf6; color: white; padding: 2px 6px; border-radius: 4px; font-size: 10.5px; font-weight: 700; }
            .badge-blocked { background: #64748b; color: white; padding: 2px 6px; border-radius: 4px; font-size: 10.5px; font-weight: 700; }
            .badge-warning { background: #f97316; color: white; padding: 2px 6px; border-radius: 4px; font-size: 10.5px; font-weight: 700; }
            
            .badge-action { background-color: #eff6ff; color: #2563eb; border: 1px solid #bfdbfe; padding: 1px 5px; border-radius: 3px; font-size: 10px; font-weight: 700; text-transform: uppercase; margin-right: 4px; display: inline-block; }
            .badge-check { background-color: #f8fafc; color: #64748b; border: 1px solid #e2e8f0; padding: 1px 5px; border-radius: 3px; font-size: 10px; font-weight: 700; text-transform: uppercase; margin-right: 4px; display: inline-block; }
            
            .outcome-category { margin-top: 3px; color: #64748b; font-size: 10px; font-weight: 600; font-family: monospace; }
            .outcome-summary { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; padding: 9px 12px; background: #f8fafc; border-left: 4px solid #cbd5e1; border-radius: 0 6px 6px 0; }
            .outcome-summary-text { color: #334155; font-size: 12.5px; word-break: break-word; font-weight: 500; }
            
            .highlight-element { background-color: #f1f5f9; color: #1e293b; border: 1px solid #cbd5e1; padding: 1px 6px; border-radius: 4px; font-weight: 600; font-size: 11.5px; display: inline-block; margin: 1px 2px; }
            .highlight-value { background-color: #f0fdf4; color: #15803d; border: 1px solid #bbf7d0; padding: 1px 6px; border-radius: 4px; font-weight: 600; font-size: 11.5px; display: inline-block; margin: 1px 2px; }
            .locator-badge { background-color: #0f172a; color: #38bdf8; border: 1px solid #334155; padding: 2px 7px; border-radius: 4px; font-family: monospace; font-size: 11px; word-break: break-all !important; display: inline-block; margin-top: 3px; }
            
            /* Failure & Soft Assertion Cards */
            .failure-detail-card { background-color: #ffffff; border: 1px solid #fecaca; border-left: 4px solid #ef4444; border-radius: 6px; margin: 4px 0; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
            .failure-header { background: #ef4444; color: white; padding: 6px 12px; font-weight: 700; font-size: 12px; display: flex; justify-content: space-between; align-items: center; }
            .soft-assert-container { padding: 10px 12px; }
            .soft-assert-item { padding: 7px 0; border-bottom: 1px solid #f1f5f9; font-size: 12px; }
            .soft-assert-item:last-child { border-bottom: none; padding-bottom: 0; }
            .soft-assert-item:first-child { padding-top: 0; }
            .soft-assert-title { font-weight: 700; color: #1e293b; margin-bottom: 2px; display: flex; align-items: center; gap: 6px; }
            .soft-assert-bullet { width: 6px; height: 6px; background-color: #ef4444; border-radius: 50%; display: inline-block; }
            .soft-assert-desc { color: #475569; margin-left: 12px; margin-bottom: 3px; font-size: 11.5px; }
            .soft-assert-diff { margin-left: 12px; display: flex; align-items: center; flex-wrap: wrap; gap: 6px; font-size: 11px; }
            .diff-expected { background: #f0fdf4; color: #166534; border: 1px solid #bbf7d0; padding: 1px 6px; border-radius: 4px; font-family: monospace; }
            .diff-arrow { color: #94a3b8; font-weight: bold; }
            .diff-actual { background: #fef2f2; color: #991b1b; border: 1px solid #fecaca; padding: 1px 6px; border-radius: 4px; font-family: monospace; font-weight: 700; }
            
            /* Minimalist Essential Debugger Cards in ELEMENT LOGS */
            .element-logs-container { display: flex; flex-direction: column; gap: 12px; }
            .dbg-summary-bar { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 6px; padding: 7px 12px; font-size: 11.5px; font-family: ui-monospace, monospace; color: #334155; display: flex; justify-content: space-between; align-items: center; }
            .dbg-card { background: #ffffff; border: 1px solid #cbd5e1; border-radius: 6px; overflow: hidden; box-shadow: 0 1px 2px rgba(0,0,0,0.03); margin-bottom: 8px; }
            .dbg-header { background: #f8fafc; border-bottom: 1px solid #e2e8f0; padding: 7px 12px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 6px; font-size: 11.5px; }
            .dbg-badge-fail { background: #dc2626; color: white; padding: 1px 6px; border-radius: 3px; font-size: 10px; font-weight: 700; font-family: ui-monospace, monospace; }
            .dbg-title { font-weight: 700; color: #0f172a; font-family: ui-monospace, monospace; font-size: 12px; }
            .dbg-sep { color: #94a3b8; margin: 0 4px; }
            .dbg-origin { color: #475569; font-family: ui-monospace, monospace; font-size: 11px; }
            .dbg-origin b { color: #0f172a; }
            .dbg-time { color: #64748b; font-family: ui-monospace, monospace; font-size: 11px; }
            
            .dbg-table { padding: 8px 12px; display: flex; flex-direction: column; gap: 6px; font-size: 11.5px; }
            .dbg-row { display: flex; align-items: baseline; gap: 10px; }
            .dbg-key { width: 140px; min-width: 140px; color: #64748b; font-weight: 600; font-size: 11px; text-transform: uppercase; }
            .dbg-val { flex: 1; color: #0f172a; font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace; font-size: 11.5px; word-break: break-all; }
            .dbg-expected { background: #f0fdf4; color: #166534; border: 1px solid #bbf7d0; padding: 1px 6px; border-radius: 4px; font-weight: 600; display: inline-block; }
            .dbg-actual { background: #fef2f2; color: #991b1b; border: 1px solid #fecaca; padding: 1px 6px; border-radius: 4px; font-weight: 700; display: inline-block; }
            .dbg-screenshot-link { color: #2563eb; font-weight: 600; text-decoration: none; display: inline-flex; align-items: center; gap: 4px; font-family: system-ui, -apple-system, sans-serif; }
            .dbg-screenshot-link:hover { text-decoration: underline; color: #1d4ed8; }
            .clean-state-box { padding: 18px 12px; text-align: center; color: #64748b; font-size: 12px; font-style: italic; background: #f8fafc; border: 1px dashed #cbd5e1; border-radius: 6px; }
            
            .dbg-section-title { font-size: 10px; font-weight: 700; color: #64748b; text-transform: uppercase; letter-spacing: 0.5px; margin-top: 6px; margin-bottom: 2px; padding-bottom: 2px; border-bottom: 1px dashed #e2e8f0; }
            .dbg-flag { background: #e2e8f0; color: #334155; padding: 1px 5px; border-radius: 3px; font-size: 10.5px; font-weight: 600; font-family: ui-monospace, monospace; margin-right: 4px; }
            .dbg-flag-true { background: #dcfce7; color: #166534; }
            .dbg-flag-false { background: #fee2e2; color: #991b1b; }
            .dom-snippet-pre { background: #0f172a; color: #38bdf8; padding: 8px 10px; border-radius: 4px; font-size: 11px; font-family: ui-monospace, monospace; overflow-x: auto; max-height: 120px; white-space: pre-wrap; word-break: break-all; margin-top: 3px; }
            
            .copy-locator-btn { background: #f1f5f9; color: #334155; border: 1px solid #cbd5e1; padding: 1px 7px; border-radius: 3px; font-size: 10.5px; font-weight: 600; cursor: pointer; transition: all 0.15s; margin-left: 6px; }
            .copy-locator-btn:hover { background: #e2e8f0; color: #0f172a; }
            
            /* Stacktrace Accordion & High-Contrast Terminal */
            .stacktrace-details { margin: 0; border-top: 1px solid #e2e8f0; background: #ffffff; overflow: hidden; }
            .stacktrace-summary { padding: 6px 12px; background: #f8fafc; font-size: 11px; font-weight: 600; color: #475569; cursor: pointer; display: flex; justify-content: space-between; align-items: center; user-select: none; transition: background 0.15s; list-style: none; }
            .stacktrace-summary::-webkit-details-marker { display: none; }
            .stacktrace-summary:hover { background: #f1f5f9; }
            .copy-stacktrace-btn { background: #ffffff; color: #4338ca; border: 1px solid #c7d2fe; padding: 1px 6px; border-radius: 3px; font-size: 10px; font-weight: 600; cursor: pointer; transition: all 0.15s; }
            .copy-stacktrace-btn:hover { background: #eef2ff; color: #3730a3; }
            .stacktrace-container { padding: 0; border-top: 1px solid #cbd5e1; }
            
            /* High-Contrast Dark Terminal Code Block */
            .code-block {
                background: #0f172a !important;
                color: #f8fafc !important;
                padding: 10px 14px !important;
                margin: 0 !important;
                font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Courier New", monospace !important;
                font-size: 11px !important;
                line-height: 1.55 !important;
                white-space: pre !important;
                max-height: 220px !important;
                overflow: auto !important;
                word-break: normal !important;
            }
            .code-block::-webkit-scrollbar { width: 6px; height: 6px; }
            .code-block::-webkit-scrollbar-track { background: #0f172a; }
            .code-block::-webkit-scrollbar-thumb { background: #334155; border-radius: 3px; }
            .code-block::-webkit-scrollbar-thumb:hover { background: #475569; }
            
            /* Syntax highlighting inside stacktrace */
            .st-header { color: #f87171 !important; font-weight: 700 !important; margin-bottom: 4px !important; }
            .st-app-frame { color: #38bdf8 !important; font-weight: 600 !important; background: rgba(56, 189, 248, 0.08) !important; border-left: 2px solid #38bdf8 !important; padding-left: 4px !important; margin: 1px 0 !important; }
            .st-sys-frame { color: #94a3b8 !important; }
            
            /* Horizontal Screenshot Gallery (solves row mb-3 stacking) */
            .test-contents > .row.mb-3:has(img) {
                display: inline-block !important;
                width: auto !important;
                margin-right: 10px !important;
                margin-bottom: 10px !important;
                vertical-align: top !important;
            }
            .test-contents > .row.mb-3:has(img) > .col-md-3 {
                max-width: 190px !important;
                width: 190px !important;
                padding: 0 !important;
            }
            .test-contents > .row.mb-3:has(img) img {
                max-height: 105px !important;
                width: 100% !important;
                object-fit: cover !important;
                border-radius: 6px !important;
                border: 1px solid #cbd5e1 !important;
                box-shadow: 0 1px 3px rgba(0,0,0,0.08) !important;
                cursor: pointer !important;
                transition: transform 0.2s ease, box-shadow 0.2s ease !important;
            }
            .test-contents > .row.mb-3:has(img) img:hover {
                transform: scale(1.03) !important;
                box-shadow: 0 4px 10px rgba(0,0,0,0.15) !important;
            }
            .test-contents > .row.mb-3:has(img) .title {
                font-size: 10px !important;
                color: #64748b !important;
                margin-top: 4px !important;
                white-space: nowrap !important;
                overflow: hidden !important;
                text-overflow: ellipsis !important;
            }
            
            /* Pure CSS controls to hide original Extent table elements and show tabbed wrapper full-width */
            table:has(> tbody > tr > td .test-report-wrapper) > thead { display: none !important; }
            table:has(> tbody > tr > td .test-report-wrapper) > tbody > tr:not(:has(.test-report-wrapper)) { display: none !important; }
            table:has(> tbody > tr > td .test-report-wrapper) > tbody > tr:has(.test-report-wrapper) > td:not(:has(.test-report-wrapper)) { display: none !important; }
            table:has(> tbody > tr > td .test-report-wrapper) > tbody > tr:has(.test-report-wrapper) > td:has(.test-report-wrapper) { padding: 0 !important; border: none !important; width: 100% !important; display: block !important; }
            """;
    }

    public static String getCustomJs() {
        return """
            function switchTab(evt, tabId) {
                const container = evt.target.closest('.test-report-wrapper');
                container.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
                container.querySelectorAll('.tab-btn').forEach(t => t.classList.remove('active'));
                container.querySelector('#' + tabId).classList.add('active');
                evt.target.classList.add('active');
            }
            
            function copyLocator(btn, text) {
                navigator.clipboard.writeText(text).then(function() {
                    const original = btn.innerText;
                    btn.innerText = 'Copied!';
                    btn.style.background = '#dcfce7';
                    btn.style.color = '#166534';
                    setTimeout(function() {
                        btn.innerText = original;
                        btn.style.background = '';
                        btn.style.color = '';
                    }, 1500);
                }).catch(function() {
                    prompt('Copy locator:', text);
                });
            }
            
            function copyStackTrace(btn, text) {
                navigator.clipboard.writeText(text).then(function() {
                    const original = btn.innerText;
                    btn.innerText = 'Copied!';
                    btn.style.background = '#dcfce7';
                    btn.style.color = '#166534';
                    setTimeout(function() {
                        btn.innerText = original;
                        btn.style.background = '';
                        btn.style.color = '';
                    }, 1500);
                }).catch(function() {
                    prompt('Copy stack trace:', text);
                });
            }
            """;
    }
}
