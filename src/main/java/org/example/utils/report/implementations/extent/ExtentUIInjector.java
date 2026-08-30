package org.example.utils.report.implementations.extent;

/**
 * Utility to provide custom CSS and JS for the ExtentReports SparkReporter.
 */
public class ExtentUIInjector {

    public static String getCustomCss() {
        return """
            .test-report-wrapper { width: 100%; }
            .tab-container { display: flex; gap: 25px; border-bottom: 2px solid #ddd; margin-bottom: 15px; }
            .tab-btn { padding: 10px 5px; cursor: pointer; font-size: 13px; font-weight: 600; color: #888; border-bottom: 3px solid transparent; }
            .tab-btn.active { color: #e74c3c; border-bottom: 3px solid #e74c3c; }
            .tab-panel { display: none; }
            .tab-panel.active { display: block; }
            .report-table { width: 100%; border-collapse: collapse; background: #fff; }
            .details-table { table-layout: fixed; width: 100%; }
            .details-table th:nth-child(1), .details-table td:nth-child(1) { width: 10% ; }
            .details-table th:nth-child(2), .details-table td:nth-child(2) { width: 13% ; }
            .details-table th:nth-child(3), .details-table td:nth-child(3) { width: 15% ; }
            .details-table th:nth-child(4), .details-table td:nth-child(4) { width: 62% ; }
            .report-table th { text-align: left; padding: 10px; background: #fafafa; border-bottom: 2px solid #eee; font-size: 12px; }
            .report-table td { padding: 10px; border-bottom: 1px solid #eee; font-size: 13px; vertical-align: top; }
            .code-block { background: #282c34; color: #abb2bf; padding: 10px; border-radius: 4px; font-family: monospace; font-size: 11px; white-space: pre-wrap; margin-top: 5px; }
            .badge-info { background: #00bcd4; color: white; padding: 2px 6px; border-radius: 3px; font-size: 11px; }
            .badge-pass { background: #00af66; color: white; padding: 2px 6px; border-radius: 3px; font-size: 11px; }
            .badge-fail { background: #e74c3c; color: white; padding: 2px 6px; border-radius: 3px; font-size: 11px; }
            .badge-skip { background: #f39c12; color: white; padding: 2px 6px; border-radius: 3px; font-size: 11px; }
            .badge-broken { background: #8e44ad; color: white; padding: 2px 6px; border-radius: 3px; font-size: 11px; }
            .badge-blocked { background: #7f8c8d; color: white; padding: 2px 6px; border-radius: 3px; font-size: 11px; }
            .badge-warning { background: #d35400; color: white; padding: 2px 6px; border-radius: 3px; font-size: 11px; }
            .outcome-category { margin-top: 4px; color: #777; font-size: 10px; word-break: break-word; }
            .outcome-summary { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; padding: 8px 10px; background: #fafafa; border-left: 4px solid #ddd; }
            .outcome-summary-text { color: #444; font-size: 13px; word-break: break-word; }
            .network-detail-label { font-weight: bold; color: #555; width: 100px; display: inline-block; }
            .highlight-element { background-color: #eef2ff; color: #3730a3; border: 1px solid #c7d2fe; padding: 1px 6px; border-radius: 4px; font-weight: 600; font-size: 11px; display: inline-block; margin: 1px 2px; }
            .highlight-value { background-color: #f0fdf4; color: #166534; border: 1px solid #bbf7d0; padding: 1px 6px; border-radius: 4px; font-weight: 600; font-size: 11px; display: inline-block; margin: 1px 2px; }
            .locator-badge { background-color: #f8fafc; color: #0f172a; border: 1px solid #cbd5e1; padding: 2px 6px; border-radius: 4px; font-family: monospace; font-size: 11px; word-break: break-all; display: inline-block; margin-top: 4px; }
            .failure-detail-card { background-color: #fff5f5; border-left: 3px solid #e74c3c; padding: 8px 12px; border-radius: 4px; margin-top: 4px; }
            .failure-title { color: #c0392b; font-weight: 600; margin-bottom: 4px; }
            .failure-reason { color: #4a5568; font-size: 12px; margin-bottom: 4px; }
            .failure-locator { color: #718096; font-size: 11px; }
            
            /* Pure CSS controls to hide original Extent table elements and show tabbed wrapper full-width */
            table:has(> tbody > tr > td .test-report-wrapper) > thead {
                display: none !important;
            }
            table:has(> tbody > tr > td .test-report-wrapper) > tbody > tr:not(:has(.test-report-wrapper)) {
                display: none !important;
            }
            table:has(> tbody > tr > td .test-report-wrapper) > tbody > tr:has(.test-report-wrapper) > td:not(:has(.test-report-wrapper)) {
                display: none !important;
            }
            table:has(> tbody > tr > td .test-report-wrapper) > tbody > tr:has(.test-report-wrapper) > td:has(.test-report-wrapper) {
                padding: 0 !important;
                border: none !important;
                width: 100% !important;
                display: block !important;
            }
            
            
                /* --- Ubah behaviour CODE BLOCK --- */
                            .code-block {
                                background: #FAFCFF;
                                color: #abb2bf;
                                padding: 10px;
                                border-radius: 4px;
                                font-family: monospace;
                                font-size: 11px;
                                white-space: pre-wrap;
                                margin-top: 5px;
                                max-height: 120px;          /* Tinggi awal saat menutup */
                                overflow: auto;
                                cursor: pointer;            /* Penanda bahwa bisa diklik */
                                word-break: break-all;      /* Supaya teks log panjang tidak meluber */
                                transition: max-height 0.3s ease-in-out; /* Animasi halus saat expand */
                            }
                            
                            .code-block.expanded {
                                max-height: 5000px !important; /* ke expand sesuai isi konten */
                            }
                            
                            /* UBAH LAYOUT WIDHT SI TAB/SEMUA TABEL REPORT */
                                .test-report-wrapper table.report-table {
                                    table-layout: fixed !important;
                                    width: 100% !important;
                                    display: table !important;
                                    border-collapse: collapse !important;
                                }
                
                                /* Kolom 1: STATUS */
                                .test-report-wrapper table.report-table th:nth-child(1),
                                .test-report-wrapper table.report-table td:nth-child(1) {
                                    width: 10% !important;
                                }
                
                                /* Kolom 2: OUTCOME */
                                .test-report-wrapper table.report-table th:nth-child(2),
                                .test-report-wrapper table.report-table td:nth-child(2) {
                                    width: 13% !important;
                                }
                
                                /* Kolom 3: TIMESTAMPS */
                                .test-report-wrapper table.report-table th:nth-child(3),
                                .test-report-wrapper table.report-table td:nth-child(3) {
                                    width: 15% !important;
                                }

                                /* Kolom 4: DETAILS / STEP DESCRIPTION */
                                .test-report-wrapper table.report-table th:nth-child(4),
                                .test-report-wrapper table.report-table td:nth-child(4) {
                                    width: 62% !important;
                                    word-break: break-all !important;      /* Ini yang menjinakkan teks Tailwind panjang */
                                    overflow-wrap: anywhere !important;
                                    white-space: normal !important;
                                }
            
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
            
                /* --- EVENT LISTENER BUAT AUTO EXPAND di CODE BLOCK --- */
                            document.addEventListener('click', function (e) {
                                if (e.target && e.target.classList.contains('code-block')) {
                                    e.target.classList.toggle('expanded');
                                }
                            });
            
            
            """;
    }
}
